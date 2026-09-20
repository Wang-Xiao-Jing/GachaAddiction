package xiaojin.gachaaddiction.config.filter;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.util.RarityUtil;

import java.util.List;
import java.util.Objects;

public final class FilterRuleMatcher {
    private FilterRuleMatcher() {
    }

    public static boolean matches(ItemStack itemStack, List<? extends List<String>> rules) {
        if (rules.isEmpty() || itemStack.isEmpty()) {
            return false;
        }
        for (List<String> rule : rules) {
            if (rule != null && !rule.isEmpty()
                    && rule.stream().allMatch(condition -> matchesCondition(itemStack, condition))) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesCondition(ItemStack itemStack, String condition) {
        boolean negated = condition.startsWith("!");
        String value = negated ? condition.substring(1) : condition;
        boolean matches;
        if (value.startsWith("#")) {
            Identifier key = Identifier.parse(value.substring(1));
            matches = itemStack.is(TagKey.create(Registries.ITEM, key));
        } else if (FilterRuleParser.isRarityCondition(value)) {
            matches = evaluateRarity(value, RarityUtil.getRarityLevel(itemStack));
        } else {
            var item = FilterRuleParser.parseItem(value);
            matches = item != null && itemStack.is(item.item()) && matchesComponents(itemStack, item.components());
        }
        return negated != matches;
    }

    private static boolean evaluateRarity(String constraint, double value) {
        int threshold = FilterRuleParser.rarityThreshold(constraint);
        return switch (FilterRuleParser.rarityOperator(constraint)) {
            case ">" -> value > threshold;
            case "<" -> value < threshold;
            case ">=" -> value >= threshold;
            case "<=" -> value <= threshold;
            case "=" -> value == threshold;
            default -> false;
        };
    }

    private static boolean matchesComponents(ItemStack itemStack, DataComponentPatch components) {
        for (var entry : components.entrySet()) {
            if (!Objects.equals(entry.getValue(), java.util.Optional.ofNullable(itemStack.get(entry.getKey())))) {
                return false;
            }
        }
        return true;
    }
}
