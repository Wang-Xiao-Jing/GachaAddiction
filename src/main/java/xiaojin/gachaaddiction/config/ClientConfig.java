package xiaojin.gachaaddiction.config;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec.*;
import org.slf4j.Logger;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.datagen.DatagenI18n;
import xiaojin.gachaaddiction.init.GachaTypes;
import xiaojin.gachaaddiction.registry.GachaaAdictionConfigData;
import xiaojin.gachaaddiction.util.RarityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientConfig extends BasicConfig {
    public static final Logger LOGGER = LogUtils.getLogger();


    //region 抽奖屏幕
    public final ConfigValue<List<? extends String>> whitelistFilterList;
    public final ConfigValue<List<? extends String>> blacklistFilterList;
    /**
     * 合并物品
     */
    public final BooleanValue mergeItem;
    /**
     * 稀有度排序
     */
    public final BooleanValue raritySorting;
    /**
     * 奖品音效
     */
    public final BooleanValue rewardSoundEffects;
    /**
     * 奖品音效优化
     */
    public final BooleanValue rewardSoundEffectsOptimize;
    /**
     * 开启FTB任务抽奖的抽奖屏幕
     */
    public final BooleanValue ftbQuestsGachaa;
    /**
     * 默认抽奖屏幕类型
     */
    public final ConfigValue<String> defaultGachaaType;
    /**
     * 老虎机配置
     */
    public final SlotMachineConfig slotMachineConfig;
    //endregion

    public ClientConfig(Builder builder) {
        super(builder);
        //region 抽奖屏幕
        push("gachaa_screen");
        whitelistFilterList = builder.comment("被这个列表匹配的物品将会触发抽奖动画，优先级大于黑名单",
                        "可使用稀有度比较 <1 =1 >1 <=1 >=1",
                        "物品标签比较 #minecraft:block",
                        "物品id比较 minecraft:acacia_planks")
                .translation(DatagenI18n.getConfigTranslation(modId, getCurrentPath("whitelist_filter_list")))
                .defineList("whitelist_filter_list", new ArrayList<>(), () -> "", ClientConfig::test);
        blacklistFilterList = builder.comment("被这个列表匹配的物品将不会触发抽奖动画",
                        "可使用稀有度比较 <1 =1 >1 <=1 >=1",
                        "物品标签比较 #minecraft:block",
                        "物品id比较 minecraft:acacia_planks")
                .translation(DatagenI18n.getConfigTranslation(modId, getCurrentPath("blacklist_filter_list")))
                .defineList("blacklist_filter_list", new ArrayList<>(), () -> "", ClientConfig::test);
        mergeItem = define(true, "merge_item");
        raritySorting = define(true, "rarity_sorting", "开启后按稀有度排序");
        rewardSoundEffects = define(true, "reward_sound_effects");
        rewardSoundEffectsOptimize = define(true, "reward_sound_effects_optimize", "开启后同时只出现一次获得奖品的音效，并只播放最高的");
        ftbQuestsGachaa = define(true, "ftb_quests_gachaa", "开启后FTB任务中就算没有开启也会开启抽奖界面");
        defaultGachaaType = builder.comment("当抽奖屏幕没有定义时将使用这个类型")
                .translation(DatagenI18n.getConfigTranslation(modId, getCurrentPath("default_gachaas_type")))
                .defineInList("default_gachaas_type", GachaTypes.SLOT_MACHINE.getId().toString(),
                        GachaTypes.getLiat().stream().map(t -> t.getId().toString()).toList());
        slotMachineConfig = new SlotMachineConfig(builder);
        pop();
        //endregion
    }

    private static boolean test(Object obj) {
        if (!(obj instanceof String v)) {
            return false;
        }
        return GachaaAdictionConfigData.TAG_PATTERN.matcher(v).matches() || GachaaAdictionConfigData.ID_PATTERN.matcher(v).matches() || GachaaAdictionConfigData.OPERATOR_RARITY_PATTERN.matcher(v).matches();
    }

    public GachaType getDefaultGachaaType() {
        return GachaTypes.getDefault(ResourceLocation.parse(defaultGachaaType.get()));
    }

    // 不是被过滤
    public boolean filter(ItemStack itemStack) {
        if (!whitelistFilterList.get().isEmpty()) {
            if (filter(itemStack, whitelistFilterList.get())) {
                return true;
            }
        }
        if (!blacklistFilterList.get().isEmpty()) {
            return !filter(itemStack, blacklistFilterList.get());
        }
        return true;
    }

    public static boolean filter(ItemStack itemStack, List<? extends String> list) {
        if (list.isEmpty() || itemStack.isEmpty()) {
            return false;
        }

        for (String v : list) {
            if (v == null || v.isEmpty() || !ClientConfig.test(v)) {
                continue;
            }

            if (v.startsWith("#") && GachaaAdictionConfigData.TAG_PATTERN.matcher(v).matches()) {
                ResourceLocation key = ResourceLocation.parse(v.substring(1));
                if (itemStack.getTags().noneMatch(itemTagKey -> itemTagKey.location().equals(key))) {
                    return false;
                }
                continue;
            }

            if (GachaaAdictionConfigData.OPERATOR_RARITY_PATTERN.matcher(v).matches()) {
                try {
                    if (!evaluate(v, RarityUtil.getRarityLevel(itemStack))) {
                        return false;
                    }
                } catch (NumberFormatException ignored) {
                }
                continue;
            }

            if (GachaaAdictionConfigData.ID_PATTERN.matcher(v).matches()) {
                ResourceLocation key = ResourceLocation.parse(v);
                Item item = BuiltInRegistries.ITEM.get(key);
                if (item != itemStack.getItem()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean evaluate(String constraint, double value) {
        String s = constraint.replaceAll("\\s+", "");

        Pattern p = Pattern.compile("^(>=|<=|>|<|=)(-?\\d+)$");
        Matcher m = p.matcher(s);

        if (!m.find()) {
            LOGGER.error("约束格式错误，必须为：运算符+整数，例如 \"<=11\": {}", constraint);
            return false;
        }

        String op = m.group(1);

        int threshold = Integer.parseInt(m.group(2));

        return switch (op) {
            case ">" -> value > threshold;
            case "<" -> value < threshold;
            case ">=" -> value >= threshold;
            case "<=" -> value <= threshold;
            case "=" -> value == threshold;
            default -> {
                LOGGER.error("不支持的运算符：{}", op);
                yield false;
            }
        };
    }

    public static class SlotMachineConfig extends BasicConfig {
        /**
         * 退出动画：每 tick 的进度推进量
         */
        public final DoubleValue exitSpeed;
        /**
         * 退出动画：最大放大倍率（1 + 此值）
         */
        public final DoubleValue exitMaxScale;
        /**
         * 拨片音效
         */
        public final BooleanValue passSoundEffects;
        /**
         * 拨片音效优化
         */
        public final BooleanValue passSoundEffectsOptimize;
        //region 物品
        /**
         * 每秒经过的物品数量
         */
        public final DoubleValue itemsPerSecond;
        /**
         * 物品垂直间距
         */
        public final DoubleValue itemSpacing;
        /**
         * 可见物品数量
         */
        public final IntValue itemVisibleCount;
        /**
         * 转盘槽位大小：decoy 生成数量范围和 result 插入位置的计算基准
         */
        public final IntValue itemSlotSize;
        /**
         * 物品最小透明度
         */
        public final DoubleValue itemMinAlpha;
        /**
         * 物品最大透明度
         */
        public final DoubleValue itemMaxAlpha;
        //endregion
        //region 减速
        /**
         * 滚动减速区：距离结果还剩几个物品时开始减速
         */
        public final DoubleValue decelZone;
        /**
         * 减速阶段最低速度倍率，防止完全停住
         */
        public final DoubleValue decelZoneMinSpeed;
        /**
         * 随机减速倍率范围：最低
         */
        public final DoubleValue decelZoneVarianceMin;
        /**
         * 随机减速倍率范围：最高
         */
        public final DoubleValue decelZoneVarianceMax;
        //endregion
        //region 速度
        /**
         * 随机速度倍率范围：最低
         */
        public final DoubleValue speedVarianceMin;
        /**
         * 随机速度倍率范围：最高
         */
        public final DoubleValue speedVarianceMax;
        /**
         * 随机最小速度倍率范围：最低
         */
        public final DoubleValue minSpeedVarianceMin;
        /**
         * 随机最小速度倍率范围：最高
         */
        public final DoubleValue minSpeedVarianceMax;

        //endregion
        private SlotMachineConfig(Builder builder) {
            super(builder);
            push("slot_machine");
            exitSpeed = define(0.1, 0.0, 1.0, "exit_speed");
            exitMaxScale = define(0.6, 0.0, 5.0, "exit_max_scale");
            passSoundEffects = define(true, "pass_sound_effects");
            passSoundEffectsOptimize = define(false, "pass_sound_effects_optimize", "开启后同时只出现一条滚动条的拨片音效");
            //region 物品
            push("item");
            itemsPerSecond = define(5.0, 0.1, 10, "items_per_second");
            itemSpacing = define(18.0, 18.0, 100.0, "item_spacing");
            itemVisibleCount = define(5, 1, 20, "visible_count");
            itemSlotSize = define(5, 1, 10, "slot_size");
            itemMinAlpha = define(0.0, 0.0, 1.0, "min_alpha");
            itemMaxAlpha = define(1.0, 0.0, 1.0, "max_alpha");
            pop();
            //endregion
            //region 减速
            push("decel_zone");
            decelZone = define(4.0, 0.0, 100.0, "decel_zone");
            decelZoneMinSpeed = define(0.05, 0.0, 1.0, "min_speed");
            decelZoneVarianceMin = define(0.5, 0.0, 10.0, "decel_zone_variance_min");
            decelZoneVarianceMax = define(1.5, 0.0, 10.0, "decel_zone_variance_max");
            pop();
            //endregion
            //region 速度
            push("speed");
            speedVarianceMin = define(0.5, 0.0, 10.0, "speed_variance_min");
            speedVarianceMax = define(1.5, 0.0, 10.0, "speed_variance_max");
            minSpeedVarianceMin = define(0.5, 0.0, 10.0, "min_speed_variance_min");
            minSpeedVarianceMax = define(1.5, 0.0, 10.0, "min_speed_variance_max");
            pop();
            //endregion
            pop();
        }
    }
}