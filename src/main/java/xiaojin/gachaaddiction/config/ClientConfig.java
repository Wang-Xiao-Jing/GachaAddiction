package xiaojin.gachaaddiction.config;

import net.neoforged.neoforge.common.ModConfigSpec.*;


public class ClientConfig extends BasicConfig {
    //region 抽奖屏幕
    /**
     * 合并物品
     */
    public final BooleanValue mergeItem;
    /**
     * 奖品音效
     */
    public final BooleanValue rewardSoundEffects;
    /**
     * 奖品音效优化
     */
    public final BooleanValue rewardSoundEffectsOptimize;
    /**
     * 老虎机配置
     */
    public final SlotMachineConfig slotMachineConfig;
    //endregion

    public ClientConfig(Builder builder) {
        super(builder);
        //region 抽奖屏幕
        push("gachaa_screen");
        mergeItem = define(true, "merge_item");
        rewardSoundEffects = define(true, "reward_sound_effects");
        rewardSoundEffectsOptimize = define(true, "reward_sound_effects_optimize", "开启后同时只出现一次获得奖品的音效，并只播放最高的");
        slotMachineConfig = new SlotMachineConfig(builder);
        pop();
        //endregion
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