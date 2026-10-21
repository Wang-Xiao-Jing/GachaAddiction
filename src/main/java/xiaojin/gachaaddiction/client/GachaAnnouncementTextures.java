package xiaojin.gachaaddiction.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.config.filter.FilterRuleMatcher;
import xiaojin.gachaaddiction.config.filter.FilterRuleParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GachaAnnouncementTextures extends SimplePreparableReloadListener<List<GachaAnnouncementTextures.Rule>> {
    public static final GachaAnnouncementTextures INSTANCE = new GachaAnnouncementTextures();
    private static final ResourceLocation RULES_FILE =
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "announcement/backgrounds.json");
    private static final String RULES_MEMBER = "rules";
    private static final String MATCH_MEMBER = "match";
    private static final String TOAST_MEMBER = "toast";
    private static final String BANNER_MEMBER = "banner";
    private static final String SPRITE_DIRECTORY = "textures/gui/sprites/";
    private static final String PNG_SUFFIX = ".png";
    private static final String INVALID_GROUP = "Expected non-empty match groups";
    private static final String INVALID_CONDITION = "Invalid announcement background condition: ";
    private static final String MISSING_BACKGROUND = "Expected toast or banner background";
    private static final String FILE_WARNING = "Cannot load announcement background rules from pack {} ({})";
    private static final String RULE_WARNING = "Skipping announcement background rule {} from pack {} ({})";
    private static final String SPRITE_WARNING = "Ignoring announcement {} background in rule {} from pack {}: {}";
    private List<Rule> rules = List.of();
    private long revision;

    private static List<List<String>> readMatch(JsonArray groups) {
        if (groups.isEmpty()) {
            throw new JsonParseException(INVALID_GROUP);
        }
        List<List<String>> result = new ArrayList<>();
        for (JsonElement element : groups) {
            JsonArray group = element.getAsJsonArray();
            if (group.isEmpty()) {
                throw new JsonParseException(INVALID_GROUP);
            }
            List<String> conditions = new ArrayList<>();
            for (JsonElement entry : group) {
                String condition = GsonHelper.convertToString(entry, MATCH_MEMBER).strip();
                String value = condition.startsWith("!") ? condition.substring(1) : condition;
                boolean valid = value.startsWith("#")
                        ? ResourceLocation.tryParse(value.substring(1)) != null
                        : FilterRuleParser.isValidCondition(condition);
                if (!valid) {
                    throw new JsonParseException(INVALID_CONDITION + condition);
                }
                if (FilterRuleParser.isRarityCondition(value)) {
                    FilterRuleParser.rarityThreshold(value);
                }
                conditions.add(condition);
            }
            result.add(List.copyOf(conditions));
        }
        return List.copyOf(result);
    }

    private static @Nullable ResourceLocation readSprite(JsonObject entry, String member,
                                                         ResourceManager resourceManager, Resource resource,
                                                         int ruleIndex) {
        if (!entry.has(member)) {
            return null;
        }
        try {
            String value = GsonHelper.getAsString(entry, member);
            ResourceLocation sprite = ResourceLocation.tryParse(value);
            if (sprite != null && resourceManager.getResource(
                    sprite.withPath(path -> SPRITE_DIRECTORY + path + PNG_SUFFIX)).isPresent()) {
                return sprite;
            }
            GachaAddiction.LOGGER.warn(SPRITE_WARNING, member, ruleIndex, resource.sourcePackId(), value);
        } catch (RuntimeException exception) {
            GachaAddiction.LOGGER.warn(SPRITE_WARNING, member, ruleIndex, resource.sourcePackId(), exception.toString());
        }
        return null;
    }

    @Override
    protected List<Rule> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        List<Rule> merged = new ArrayList<>();
        List<Resource> resources = resourceManager.getResourceStack(RULES_FILE);
        // 原版资源栈从低到高排列；匹配时需要先检查高优先级包。
        for (int packIndex = resources.size() - 1; packIndex >= 0; packIndex--) {
            Resource resource = resources.get(packIndex);
            try (var reader = resource.openAsReader()) {
                JsonArray entries = GsonHelper.getAsJsonArray(GsonHelper.parse(reader), RULES_MEMBER);
                for (int ruleIndex = 0; ruleIndex < entries.size(); ruleIndex++) {
                    try {
                        JsonObject entry = entries.get(ruleIndex).getAsJsonObject();
                        List<List<String>> match = readMatch(GsonHelper.getAsJsonArray(entry, MATCH_MEMBER));
                        ResourceLocation toast = readSprite(entry, TOAST_MEMBER, resourceManager, resource, ruleIndex);
                        ResourceLocation banner = readSprite(entry, BANNER_MEMBER, resourceManager, resource, ruleIndex);
                        if (toast == null && banner == null) {
                            throw new JsonParseException(MISSING_BACKGROUND);
                        }
                        merged.add(new Rule(match, toast, banner));
                    } catch (RuntimeException exception) {
                        GachaAddiction.LOGGER.warn(RULE_WARNING, ruleIndex, resource.sourcePackId(), RULES_FILE, exception);
                    }
                }
            } catch (IOException | RuntimeException exception) {
                GachaAddiction.LOGGER.warn(FILE_WARNING, resource.sourcePackId(), RULES_FILE, exception);
            }
        }
        return List.copyOf(merged);
    }

    @Override
    protected void apply(List<Rule> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        rules = prepared;
        revision++;
    }

    public long revision() {
        return revision;
    }

    public Backgrounds select(ItemStack item, ResourceLocation defaultToast, ResourceLocation defaultBanner) {
        ResourceLocation toast = null;
        ResourceLocation banner = null;
        for (Rule rule : rules) {
            if ((toast != null || rule.toast == null) && (banner != null || rule.banner == null)
                    || !FilterRuleMatcher.matches(item, rule.match)) {
                continue;
            }
            if (toast == null) {
                toast = rule.toast;
            }
            if (banner == null) {
                banner = rule.banner;
            }
            if (toast != null && banner != null) {
                break;
            }
        }
        return new Backgrounds(toast == null ? defaultToast : toast, banner == null ? defaultBanner : banner);
    }

    public record Backgrounds(ResourceLocation toast, ResourceLocation banner) {
    }

    record Rule(List<List<String>> match, @Nullable ResourceLocation toast, @Nullable ResourceLocation banner) {
    }
}
