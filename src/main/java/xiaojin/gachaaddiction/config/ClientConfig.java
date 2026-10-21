package xiaojin.gachaaddiction.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.ModConfigSpec;
import xiaojin.gachaaddiction.api.GachaSecondaryType;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.config.entry.*;
import xiaojin.gachaaddiction.config.filter.FilterRuleMatcher;
import xiaojin.gachaaddiction.config.filter.FilterRuleParser;
import xiaojin.gachaaddiction.registry.GachaTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientConfig extends BasicConfig {
    private static final int DEFAULT_MINIMUM_REWARD_RARITY = Rarity.RARE.ordinal();
    private static final String MINIMUM_REWARD_RARITY_KEY = "minimum_reward_rarity";
    private static final String ANNOUNCEMENT_SECTION = "announcement";
    private static final int DEFAULT_ANNOUNCEMENT_SECONDS = 8;
    private static final int MAXIMUM_ANNOUNCEMENT_SECONDS = 60;
    private static final String WHITELIST_COMMENT = "Items matched by this list trigger the gacha animation and take priority over the blacklist.";
    private static final String BLACKLIST_COMMENT = "Items matched by this list do not trigger the gacha animation.";
    private static final String COMPOUND_RULE_COMMENT = "Separate compound conditions with ; and compare rarity with <1, =1, >1, <=1, or >=1.";
    private static final String TAG_RULE_COMMENT = "Match item tags with #minecraft:block and exclude tags with !#.";
    private static final String ITEM_RULE_COMMENT = "Use vanilla item command syntax and exclude items with !.";
    private static final String RARITY_SORTING_COMMENT = "Sort rewards by rarity when enabled.";
    private static final String REWARD_SOUND_OPTIMIZATION_COMMENT = "Only play the highest-rarity reward sound when multiple rewards appear together.";
    private static final String FTB_QUESTS_GACHA_COMMENT = "Open the gacha screen for FTB Quest rewards even when the quest does not enable it.";
    private static final String DEFAULT_GACHA_TYPE_COMMENT = "Use this type when the gacha screen type is not defined.";
    private static final String PASS_SOUND_OPTIMIZATION_COMMENT = "Only play the passing sound from one reel at a time.";
    private static final String LDLIB2_TEMPLATE_COMMENT = "LDLib2 is optional. This client resource-pack template is used only by the selected LDLib2 gacha interface.";
    private static final String LDLIB2_STYLESHEET_COMMENT = "Optional client resource-pack LSS stylesheet for the LDLib2 gacha interface.";

    //region 抽奖屏幕
    public final FilterListConfigEntry whitelistFilterList;
    public final FilterListConfigEntry blacklistFilterList;
    public final IntConfigEntry minimumRewardRarity;
    public final BooleanConfigEntry protectLoot;
    public final SelectorConfigEntry<GachaSecondaryType> secondaryType;
    public final IntConfigEntry choiceCount;
    public final BooleanConfigEntry automaticAnnouncements;
    public final BooleanConfigEntry announcementChat;
    public final BooleanConfigEntry announcementToast;
    public final BooleanConfigEntry announcementBanner;
    public final BooleanConfigEntry announcementTooltips;
    public final IntConfigEntry announcementSeconds;
    /**
     * 合并物品
     */
    public final BooleanConfigEntry mergeItem;
    /**
     * 稀有度排序
     */
    public final BooleanConfigEntry raritySorting;
    /**
     * 奖品音效
     */
    public final BooleanConfigEntry rewardSoundEffects;
    /**
     * 奖品音效优化
     */
    public final BooleanConfigEntry rewardSoundEffectsOptimize;
    /**
     * 开启FTB任务抽奖的抽奖屏幕
     */
    public final BooleanConfigEntry ftbQuestsGachaa;
    /**
     * 默认抽奖屏幕类型
     */
    public final SelectorConfigEntry<GachaType> defaultGachaaType;
    /**
     * 老虎机配置
     */
    public final SlotMachineConfig slotMachineConfig;
    public final StringConfigEntry ldlib2GachaTemplate;
    public final StringConfigEntry ldlib2GachaStyleSheet;
    //endregion

    public ClientConfig(ModConfigSpec.Builder builder) {
        super(builder);
        //region 抽奖屏幕
        push("gachaa_screen");
        secondaryType = selectorDefine(GachaSecondaryType.NONE, List.of(GachaSecondaryType.values()), "secondary_type",
                GachaSecondaryType::byName, GachaSecondaryType::getSerializedName);
        choiceCount = define(GachaSecondaryType.DEFAULT_CHOICES, 2, GachaSecondaryType.MAXIMUM_CHOICES, "choice_count");
        protectLoot = define(true, "protect_loot", "Request exclusive access to the source while its gacha animation is playing.");
        whitelistFilterList = defineFilterList(new ArrayList<>(), ClientConfig::testCondition, "whitelist_filter_list",
                WHITELIST_COMMENT, COMPOUND_RULE_COMMENT, TAG_RULE_COMMENT, ITEM_RULE_COMMENT);
        blacklistFilterList = defineFilterList(new ArrayList<>(), ClientConfig::testCondition, "blacklist_filter_list",
                BLACKLIST_COMMENT, COMPOUND_RULE_COMMENT, TAG_RULE_COMMENT, ITEM_RULE_COMMENT);
        minimumRewardRarity = define(DEFAULT_MINIMUM_REWARD_RARITY, Rarity.COMMON.ordinal(),
                Integer.MAX_VALUE, MINIMUM_REWARD_RARITY_KEY);
        mergeItem = define(true, "merge_item");
        raritySorting = define(true, "rarity_sorting", RARITY_SORTING_COMMENT);
        rewardSoundEffects = define(true, "reward_sound_effects");
        rewardSoundEffectsOptimize = define(true, "reward_sound_effects_optimize", REWARD_SOUND_OPTIMIZATION_COMMENT);
        ftbQuestsGachaa = define(true, "ftb_quests_gachaa", FTB_QUESTS_GACHA_COMMENT);
        defaultGachaaType = selectorDefine(GachaTypes.MODERN,
                GachaTypes.getAll(), "default_gachaas_type",
                v -> Objects.requireNonNull(GachaTypes.get(ResourceLocation.parse(v))),
                v -> Objects.requireNonNull(Objects.requireNonNull(v).getId()).toString(),
                DEFAULT_GACHA_TYPE_COMMENT);
        slotMachineConfig = new SlotMachineConfig(this);
        push("ldlib2");
        ldlib2GachaTemplate = register(StringConfigEntry.builder(builder, modId, currentPath)
                .key("template").comments(LDLIB2_TEMPLATE_COMMENT)
                .defaultValue(GachaPresentationDefaults.DEFAULT_TEMPLATE_ID.toString())
                .validator(value -> ResourceLocation.tryParse(value) != null).build());
        ldlib2GachaStyleSheet = register(StringConfigEntry.builder(builder, modId, currentPath)
                .key("style_sheet").comments(LDLIB2_STYLESHEET_COMMENT)
                .defaultValue(GachaPresentationDefaults.DEFAULT_STYLESHEET_ID.toString())
                .validator(value -> ResourceLocation.tryParse(value) != null).build());
        pop();
        pop();
        //endregion
        push(ANNOUNCEMENT_SECTION);
        automaticAnnouncements = define(true, "automatic");
        announcementChat = define(true, "chat");
        announcementToast = define(false, "toast");
        announcementBanner = define(false, "banner");
        announcementTooltips = define(true, "tooltips");
        announcementSeconds = define(DEFAULT_ANNOUNCEMENT_SECONDS, 1, MAXIMUM_ANNOUNCEMENT_SECONDS, "seconds");
        pop();
    }

    private static boolean testCondition(Object obj) {
        return obj instanceof String condition && FilterRuleParser.isValidCondition(condition);
    }

    public GachaType getDefaultGachaaType() {
        return defaultGachaaType.get();
    }

    // 不是被过滤
    public boolean filter(ItemStack itemStack) {
        if (!whitelistFilterList.get().isEmpty()) {
            if (FilterRuleMatcher.matches(itemStack, whitelistFilterList.get())) {
                return true;
            }
        }
        if (!blacklistFilterList.get().isEmpty()) {
            return !FilterRuleMatcher.matches(itemStack, blacklistFilterList.get());
        }
        return true;
    }

    public static class SlotMachineConfig {
        /**
         * 退出动画：每 tick 的进度推进量
         */
        public final DoubleConfigEntry exitSpeed;
        /**
         * 退出动画：最大放大倍率（1 + 此值）
         */
        public final DoubleConfigEntry exitMaxScale;
        /**
         * 拨片音效
         */
        public final BooleanConfigEntry passSoundEffects;
        /**
         * 拨片音效优化
         */
        public final BooleanConfigEntry passSoundEffectsOptimize;
        //region 物品
        /**
         * 每秒经过的物品数量
         */
        public final DoubleConfigEntry itemsPerSecond;
        /**
         * 物品垂直间距
         */
        public final DoubleConfigEntry itemSpacing;
        /**
         * 可见物品数量
         */
        public final IntConfigEntry itemVisibleCount;
        /**
         * 转盘槽位大小：decoy 生成数量范围和 result 插入位置的计算基准
         */
        public final IntConfigEntry itemSlotSize;
        /**
         * 物品最小透明度
         */
        public final DoubleConfigEntry itemMinAlpha;
        /**
         * 物品最大透明度
         */
        public final DoubleConfigEntry itemMaxAlpha;
        //endregion
        //region 减速
        /**
         * 滚动减速区：距离结果还剩几个物品时开始减速
         */
        public final DoubleConfigEntry decelZone;
        /**
         * 减速阶段最低速度倍率，防止完全停住
         */
        public final DoubleConfigEntry decelZoneMinSpeed;
        /**
         * 随机减速倍率范围：最低
         */
        public final DoubleConfigEntry decelZoneVarianceMin;
        /**
         * 随机减速倍率范围：最高
         */
        public final DoubleConfigEntry decelZoneVarianceMax;
        //endregion
        //region 速度
        /**
         * 随机速度倍率范围：最低
         */
        public final DoubleConfigEntry speedVarianceMin;
        /**
         * 随机速度倍率范围：最高
         */
        public final DoubleConfigEntry speedVarianceMax;
        /**
         * 随机最小速度倍率范围：最低
         */
        public final DoubleConfigEntry minSpeedVarianceMin;
        /**
         * 随机最小速度倍率范围：最高
         */
        public final DoubleConfigEntry minSpeedVarianceMax;
        //endregion

        SlotMachineConfig(BasicConfig config) {
            //region 老虎机
            config.push("slot_machine");
            exitSpeed = config.define(0.1, 0.0, 1.0, "exit_speed");
            exitMaxScale = config.define(0.6, 0.0, 5.0, "exit_max_scale");
            passSoundEffects = config.define(true, "pass_sound_effects");
            passSoundEffectsOptimize = config.define(false, "pass_sound_effects_optimize", PASS_SOUND_OPTIMIZATION_COMMENT);
            //endregion
            //region 物品
            config.push("item");
            itemsPerSecond = config.define(5.0, 0.1, 10, "items_per_second");
            itemSpacing = config.define(18.0, 18.0, 100.0, "item_spacing");
            itemVisibleCount = config.define(5, 1, 20, "visible_count");
            itemSlotSize = config.define(5, 1, 10, "slot_size");
            itemMinAlpha = config.define(0.0, 0.0, 1.0, "min_alpha");
            itemMaxAlpha = config.define(1.0, 0.0, 1.0, "max_alpha");
            config.pop();
            //endregion
            //region 减速
            config.push("decel_zone");
            decelZone = config.define(4.0, 0.0, 100.0, "decel_zone");
            decelZoneMinSpeed = config.define(0.05, 0.0, 1.0, "min_speed");
            decelZoneVarianceMin = config.define(0.5, 0.0, 10.0, "decel_zone_variance_min");
            decelZoneVarianceMax = config.define(1.5, 0.0, 10.0, "decel_zone_variance_max");
            config.pop();
            //endregion
            //region 速度
            config.push("speed");
            speedVarianceMin = config.define(0.5, 0.0, 10.0, "speed_variance_min");
            speedVarianceMax = config.define(1.5, 0.0, 10.0, "speed_variance_max");
            minSpeedVarianceMin = config.define(0.5, 0.0, 10.0, "min_speed_variance_min");
            minSpeedVarianceMax = config.define(1.5, 0.0, 10.0, "min_speed_variance_max");
            config.pop();
            config.pop();
            //endregion
        }
    }
}
