package xiaojin.gachaaddiction.util;

import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.generator.lang.LangHandler;

public interface TranslationKeys {
    //region 命令
    String COMMANDS_GACHA_INVALID_LOOT_TABLE = LangHandler.creates(GachaAddiction.MODID, "commands.gachaaddiction.gacha.invalid_loot_table",
            "Invalid loot table: %s", "无效的战利品表: %s");
    String COMMANDS_GACHA_INVALID_TYPE = LangHandler.creates(GachaAddiction.MODID, "commands.gachaaddiction.gacha.invalid_type",
            "Invalid gacha type: %s", "无效的抽奖类型: %s");
    String COMMANDS_GACHA_SUCCESS = LangHandler.creates(GachaAddiction.MODID, "commands.gachaaddiction.gacha.success",
            "Executed %s times for %s players from loot table: %s", "已为 %s 名玩家执行 %s 次战利品表: %s");
    //endregion

    //region 外部约定键
    @SuppressWarnings("unused")
    String FTBQUESTS_REWARD_FTBQUESTS_LOOT_GACHA_TYPE = LangHandler.creates(GachaAddiction.MODID, "ftbquests.reward.ftbquests.loot.gachaaddiction:gacha_type",
            "Gacha Style", "抽卡样式");
    //endregion

    //region 抽奖类型
    @SuppressWarnings("unused")
    String GACHA_TYPE_EMPTY = LangHandler.creates(GachaAddiction.MODID, "gacha_type.gachaaddiction:empty",
            "None", "无");
    @SuppressWarnings("unused")
    String GACHA_TYPE_SLOT_MACHINE = LangHandler.creates(GachaAddiction.MODID, "gacha_type.gachaaddiction:slot_machine",
            "Slot Machine", "老虎机");
    //endregion

    //region 配置
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen"),
            "Gacha Screen", "抽奖屏幕");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_BLACKLIST_FILTER_LIST = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "blacklist_filter_list"),
            "Blacklist Filter", "黑名单过滤");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_BLACKLIST_FILTER_LIST_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "blacklist_filter_list", "tooltip"),
            "Items matching this list are filtered out. Use ; for compound conditions, #tag/!#tag for tags, rarity comparisons, and vanilla item syntax with optional ! exclusion.",
            "匹配此列表的物品将被过滤。使用 ; 组合条件，使用 #标签/!#标签匹配或排除标签，支持稀有度比较和带可选 ! 排除的原版物品格式。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_DEFAULT_GACHAAS_TYPE = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "default_gachaas_type"),
            "Default Gacha Screen Type", "默认抽奖屏幕类型");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_DEFAULT_GACHAAS_TYPE_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "default_gachaas_type", "tooltip"),
            "Used when the gacha data does not specify a screen type.", "当抽奖数据没有指定屏幕类型时，使用该类型。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_FTB_QUESTS_GACHAA = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "ftb_quests_gachaa"),
            "Enable Gacha Screen for FTB Quest Rewards", "开启FTB任务抽奖的抽奖屏幕");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_FTB_QUESTS_GACHAA_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "ftb_quests_gachaa", "tooltip"),
            "Open the gacha screen for FTB quest rewards even when the quest does not enable it.", "开启后，即使任务未开启也会为 FTB 任务奖励打开抽奖界面。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_MERGE_ITEM = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "merge_item"),
            "Merge Duplicate Rewards", "合并抽奖物品");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_MERGE_ITEM_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "merge_item", "tooltip"),
            "Display identical reward items together in the same slot.", "开启后，同种物品会合并显示在同一个槽位中。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_RARITY_SORTING = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "rarity_sorting"),
            "Sort by Rarity", "稀有度排序");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_RARITY_SORTING_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "rarity_sorting", "tooltip"),
            "Sort rewards by rarity from highest to lowest.", "按稀有度从高到低排序奖品。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_RARITY_FROM_LOOT_TABLE = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "rarity_from_loot_table"),
            "Rarity from Loot Table", "按战利品表判定稀有度");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_RARITY_FROM_LOOT_TABLE_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "rarity_from_loot_table", "tooltip"),
            "Rank rarity by how likely each item is inside the container's own loot table instead of the item's vanilla rarity.",
            "根据物品在该容器战利品表中的出现概率判定稀有度，而不使用原版稀有度。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_REWARD_SOUND_EFFECTS = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "reward_sound_effects"),
            "Reward Sound Effects", "奖品音效");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_REWARD_SOUND_EFFECTS_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "reward_sound_effects", "tooltip"),
            "Play a sound when a reward is drawn.", "抽到奖品时播放音效。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_REWARD_SOUND_EFFECTS_OPTIMIZE = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "reward_sound_effects_optimize"),
            "Reward Sound Effect Optimization", "奖品音效优化");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_REWARD_SOUND_EFFECTS_OPTIMIZE_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "reward_sound_effects_optimize", "tooltip"),
            "Avoid overlapping reward sounds by only playing the highest rarity sound.", "优化奖品音效播放，避免多个音效叠加，只播放最高稀有度的。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine"),
            "Slot Machine", "老虎机");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "decel_zone"),
            "Deceleration", "减速");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone"),
            "Deceleration Zone Distance", "滚动减速区距离");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone", "tooltip"),
            "Start decelerating when this many items remain before the result.", "距离结果还剩几个物品时开始减速。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE_VARIANCE_MAX = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone_variance_max"),
            "Max Random Deceleration Multiplier", "随机减速倍率范围最高");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE_VARIANCE_MAX_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone_variance_max", "tooltip"),
            "Upper bound of the random deceleration multiplier.", "随机减速倍率范围上限，让减速效果带随机性。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE_VARIANCE_MIN = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone_variance_min"),
            "Min Random Deceleration Multiplier", "随机减速倍率范围最低");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE_VARIANCE_MIN_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone_variance_min", "tooltip"),
            "Lower bound of the random deceleration multiplier.", "随机减速倍率范围下限，让减速效果带随机性。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_MIN_SPEED = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "decel_zone", "min_speed"),
            "Minimum Speed Multiplier in Deceleration Phase", "减速阶段最低速度倍率");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_MIN_SPEED_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "decel_zone", "min_speed", "tooltip"),
            "Minimum speed multiplier during deceleration to prevent the reel from stopping completely.", "减速阶段最低速度倍率，防止转盘完全停住。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_EXIT_MAX_SCALE = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "exit_max_scale"),
            "Exit Animation Max Scale", "退出动画最大放大倍率");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_EXIT_MAX_SCALE_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "exit_max_scale", "tooltip"),
            "Maximum exit animation scale increase (actual scale is 1 plus this value).", "退出动画最大放大倍率（实际为 1 + 此值）。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_EXIT_SPEED = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "exit_speed"),
            "Exit Animation Progress Per Tick", "退出动画每tick进度推进量");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_EXIT_SPEED_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "exit_speed", "tooltip"),
            "Exit animation progress per tick. Higher values exit faster.", "退出动画每 tick 的进度推进量，越大退出越快。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item"),
            "Item", "物品");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_ITEM_SPACING = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "item_spacing"),
            "Item Vertical Spacing", "物品垂直间距");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_ITEM_SPACING_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "item_spacing", "tooltip"),
            "Vertical spacing between items in pixels.", "物品之间的垂直间距（像素）。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_ITEMS_PER_SECOND = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "items_per_second"),
            "Items Per Second", "每秒经过的物品数量");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_ITEMS_PER_SECOND_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "items_per_second", "tooltip"),
            "Number of items passing per second. Higher values scroll faster.", "转盘每秒经过的物品数量，越大滚动越快。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_MAX_ALPHA = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "max_alpha"),
            "Max Item Transparency", "物品最大透明度");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_MAX_ALPHA_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "max_alpha", "tooltip"),
            "Maximum opacity of items near the center (0-1).", "靠近中心的物品的最大透明度（0-1）。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_MIN_ALPHA = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "min_alpha"),
            "Min Item Transparency", "物品最小透明度");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_MIN_ALPHA_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "min_alpha", "tooltip"),
            "Minimum opacity of items far from the center (0-1).", "远离中心的物品的最小透明度（0-1）。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_SLOT_SIZE = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "slot_size"),
            "Slot Size", "转盘槽位大小");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_SLOT_SIZE_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "slot_size", "tooltip"),
            "Reel slot size used to calculate the decoy count range and result insertion position.", "转盘槽位大小，是 decoy 生成数量范围和 result 插入位置的计算基准。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_VISIBLE_COUNT = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "visible_count"),
            "Visible Item Count", "可见物品数量");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_VISIBLE_COUNT_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "item", "visible_count", "tooltip"),
            "Number of items visible on the reel at once.", "转盘上同时可见的物品数量。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_PASS_SOUND_EFFECTS = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "pass_sound_effects"),
            "Tick Sound Effects", "拨片音效");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_PASS_SOUND_EFFECTS_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "pass_sound_effects", "tooltip"),
            "Play a ticking sound as items scroll past.", "物品滚过时播放拨片音效。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_PASS_SOUND_EFFECTS_OPTIMIZE = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "pass_sound_effects_optimize"),
            "Tick Sound Effect Optimization", "拨片音效优化");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_PASS_SOUND_EFFECTS_OPTIMIZE_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "pass_sound_effects_optimize", "tooltip"),
            "Only play the ticking sound from one reel at a time.", "开启后同时只出现一条滚动条的拨片音效。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "speed"),
            "Speed", "速度");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_MIN_SPEED_VARIANCE_MAX = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "speed", "min_speed_variance_max"),
            "Max Random Minimum Speed Multiplier", "随机最小速度倍率范围最高");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_MIN_SPEED_VARIANCE_MAX_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "speed", "min_speed_variance_max", "tooltip"),
            "Upper bound of the random minimum speed multiplier.", "随机最小速度倍率范围上限。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_MIN_SPEED_VARIANCE_MIN = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "speed", "min_speed_variance_min"),
            "Min Random Minimum Speed Multiplier", "随机最小速度倍率范围最低");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_MIN_SPEED_VARIANCE_MIN_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "speed", "min_speed_variance_min", "tooltip"),
            "Lower bound of the random minimum speed multiplier.", "随机最小速度倍率范围下限。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_SPEED_VARIANCE_MAX = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "speed", "speed_variance_max"),
            "Max Random Speed Multiplier", "随机速度倍率范围最高");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_SPEED_VARIANCE_MAX_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "speed", "speed_variance_max", "tooltip"),
            "Upper bound of the random speed multiplier.", "随机速度倍率范围上限，让滚动速度带随机性。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_SPEED_VARIANCE_MIN = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "speed", "speed_variance_min"),
            "Min Random Speed Multiplier", "随机速度倍率范围最低");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_SPEED_VARIANCE_MIN_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "slot_machine", "speed", "speed_variance_min", "tooltip"),
            "Lower bound of the random speed multiplier.", "随机速度倍率范围下限，让滚动速度带随机性。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_WHITELIST_FILTER_LIST = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "whitelist_filter_list"),
            "Whitelist Filter", "白名单过滤");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_WHITELIST_FILTER_LIST_TOOLTIP = LangHandler.creates(GachaAddiction.MODID, LangHandler.getConfigTranslation(GachaAddiction.MODID, "gachaa_screen", "whitelist_filter_list", "tooltip"),
            "Items matching this list take priority over the blacklist. Use ; for compound conditions, #tag/!#tag for tags, rarity comparisons, and vanilla item syntax with optional ! exclusion.",
            "匹配此列表的物品优先于黑名单。使用 ; 组合条件，使用 #标签/!#标签匹配或排除标签，支持稀有度比较和带可选 ! 排除的原版物品格式。");
    //endregion

    //region 配置界面
    String GUI_CONFIG_EMPTY = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.config.empty",
            "No configuration available", "暂无配置");
    String GUI_CONFIG_TITLE = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.config.title",
            "GachaAddiction Configuration", "GachaAddiction 配置");
    String GUI_CONFIG_UNSUPPORTED = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.config.unsupported",
            "Unsupported configuration type: %s", "暂不支持此配置类型：%s");
    String GUI_CONFIG_CONFIRM = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.config.confirm",
            "Confirm", "确认");
    String GUI_CONFIG_CANCEL = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.config.cancel",
            "Cancel", "取消");
    String GUI_CONFIG_LIST_INVALID = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.config.list_invalid",
            "Complete or remove invalid entries before confirming", "确认前请补全或删除无效条目");
    String GUI_CONFIG_FILTER_RULE_TITLE = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.config.filter_rule_title",
            "Filter Conditions", "过滤条件");
    String GUI_CONFIG_FILTER_RULE_EMPTY = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.config.filter_rule_empty",
            "Add at least one filter condition", "请至少添加一个过滤条件");
    String GUI_CONFIG_FILTER_CONDITION_EMPTY = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.config.filter_condition_empty",
            "Enter one filter condition. Item components use component=value or !component. Add compound conditions as separate entries in the same rule. Examples:\nminecraft:diamond\nminecraft:flow_banner_pattern[minecraft:custom_data={adadada:{adda:1b}}]\n#minecraft:logs\nrarity>=2\n!minecraft:stone\n!#minecraft:logs",
            "请输入一个过滤条件。物品组件使用 组件=值 或 !组件；同一规则的复合条件请分别添加为多个条目。格式示例：\nminecraft:diamond\nminecraft:flow_banner_pattern[minecraft:custom_data={adadada:{adda:1b}}]\n#minecraft:logs\nrarity>=2\n!minecraft:stone\n!#minecraft:logs");
    String GUI_CONFIG_FILTER_CONDITION_INVALID = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.config.filter_condition_invalid",
            "Invalid filter condition. Item components require component=value or !component. Add compound conditions as separate entries in the same rule. Examples:\nminecraft:diamond\nminecraft:flow_banner_pattern[minecraft:custom_data={adadada:{adda:1b}}]\n#minecraft:logs\nrarity>=2\n!minecraft:stone\n!#minecraft:logs",
            "过滤条件格式错误。物品组件必须使用 组件=值 或 !组件；同一规则的复合条件请分别添加为多个条目。示例：\nminecraft:diamond\nminecraft:flow_banner_pattern[minecraft:custom_data={adadada:{adda:1b}}]\n#minecraft:logs\nrarity>=2\n!minecraft:stone\n!#minecraft:logs");
    //endregion

    //region 抽奖界面
    String GUI_SLOT_MACHINES_KIP_HINT = LangHandler.creates(GachaAddiction.MODID, "gui.gachaaddiction.slot_machines.kip_hint",
            "Press SPACE to skip or ESC to open the container GUI directly", "按 空格 键进行跳过或按 ESC 键直接打开容器GUI");
    //endregion

    //region 资源包
    @SuppressWarnings("unused")
    String PACK_DESCRIPTION = LangHandler.creates(GachaAddiction.MODID, "pack.gachaaddiction.description",
            "Gacha Addiction", "抽卡瘾");
    //endregion

    static void init() {
    }
}
