package xiaojin.gachaaddiction.config.filter;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import xiaojin.gachaaddiction.registry.GachaaAdictionConfigData;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class FilterRuleParser {
    private static final ItemParser ITEM_PARSER = new ItemParser(
            RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    private static final Pattern RARITY_PATTERN = Pattern.compile("^(>=|<=|>|<|=)?(-?\\d+)$");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private FilterRuleParser() {
    }

    public static boolean isValidCondition(String condition) {
        String value = condition.startsWith("!") ? condition.substring(1) : condition;
        if (value.isEmpty()) {
            return false;
        }
        if (value.startsWith("#")) {
            return GachaaAdictionConfigData.TAG_PATTERN.matcher(value).matches();
        }
        if (isRarityCondition(value)) {
            return !condition.startsWith("!");
        }
        return isValidItemCondition(value);
    }

    public static boolean isRarityCondition(String value) {
        return RARITY_PATTERN.matcher(normalizeRarity(value)).matches();
    }

    public static int rarityThreshold(String value) {
        var matcher = RARITY_PATTERN.matcher(normalizeRarity(value));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid rarity condition");
        }
        return Integer.parseInt(matcher.group(2));
    }

    public static String rarityOperator(String value) {
        var matcher = RARITY_PATTERN.matcher(normalizeRarity(value));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid rarity condition");
        }
        return matcher.group(1) == null ? "=" : matcher.group(1);
    }

    public static ItemParser.ItemResult parseItem(String value) {
        StringReader reader = new StringReader(value);
        try {
            ItemParser.ItemResult item = ITEM_PARSER.parse(reader);
            return reader.canRead() ? null : item;
        } catch (CommandSyntaxException ignored) {
            return null;
        }
    }

    private static String normalizeRarity(String value) {
        return WHITESPACE_PATTERN.matcher(value).replaceAll("");
    }

    private static boolean isValidItemCondition(String value) {
        int componentsStart = value.indexOf('[');
        String itemId = componentsStart < 0 ? value : value.substring(0, componentsStart);
        Identifier id = Identifier.tryParse(itemId);
        if (id == null || itemId.isEmpty()) {
            return false;
        }
        if (BuiltInRegistries.ITEM.containsKey(id)) {
            return parseItem(value) != null;
        }
        return componentsStart < 0 || hasValidComponentStructure(value, componentsStart);
    }

    private static boolean hasValidComponentStructure(String value, int componentsStart) {
        if (componentsStart != value.indexOf('[') || componentsStart == value.length() - 1) {
            return false;
        }
        List<Character> delimiters = new ArrayList<>();
        char quote = 0;
        boolean escaped = false;
        for (int i = componentsStart; i < value.length(); i++) {
            char character = value.charAt(i);
            if (quote != 0) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == quote) {
                    quote = 0;
                }
                continue;
            }
            if (character == '\'' || character == '"') {
                quote = character;
            } else if (character == '[' || character == '{' || character == '(') {
                delimiters.add(character);
            } else if (character == ']' || character == '}' || character == ')') {
                if (delimiters.isEmpty() || !isMatchingDelimiter(delimiters.removeLast(), character)) {
                    return false;
                }
                if (delimiters.isEmpty() && i != value.length() - 1) {
                    return false;
                }
            }
        }
        return quote == 0 && delimiters.isEmpty() && value.endsWith("]");
    }

    private static boolean isMatchingDelimiter(char opening, char closing) {
        return opening == '[' && closing == ']'
                || opening == '{' && closing == '}'
                || opening == '(' && closing == ')';
    }
}
