package xiaojin.gachaaddiction.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.client.gui.screen.SlotMachineScreen;
import xiaojin.gachaaddiction.config.ClientConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModZhCn extends DatagenI18n {
    private static final Map<Supplier<? extends SoundEvent>, String> SOUND_EVENT = new HashMap<>();
    private static final Map<String, String> MAP = new HashMap<>();

    public ModZhCn(PackOutput output) {
        super(output, GachaAddiction.MODID, "zh_cn");
    }

    @Override
    public void addTranslations() {
        super.addTranslations();

        addPackDescription(GachaAddiction.MODID, "抽卡瘾");
        MAP.forEach(this::add);
        addSoundEventList(SOUND_EVENT);
        add(SlotMachineScreen.SKIP_HINT, "按 空格 键进行跳过或按 ESC 键直接打开容器GUI");
        add(GachaAddiction.MODID + ".configuration.gachaa_screen", "抽奖屏幕");
        ClientConfig clientConfig = GachaAddictionConfig.CLIENT;
        add(clientConfig.mergeItem, "合并抽奖物品");
        add(clientConfig.raritySorting, "稀有度排序");
        add(clientConfig.rewardSoundEffects, "奖品音效");
        add(clientConfig.rewardSoundEffectsOptimize, "奖品音效优化");
        add(clientConfig.ftbQuestsGachaa, "开启FTB任务抽奖的抽奖屏幕");
        add(clientConfig.defaultGachaaType, "默认抽奖屏幕类型");

        ClientConfig.SlotMachineConfig slotMachineConfig = clientConfig.slotMachineConfig;
        add(GachaAddiction.MODID + ".configuration.gachaa_screen.slot_machine", "老虎机");
        add(GachaAddiction.MODID + ".configuration.gachaa_screen.slot_machine.item", "物品");
        add(GachaAddiction.MODID + ".configuration.gachaa_screen.slot_machine.decel_zone", "减速");
        add(GachaAddiction.MODID + ".configuration.gachaa_screen.slot_machine.speed", "速度");
        add(slotMachineConfig.itemsPerSecond, "每秒经过的物品数量");
        add(slotMachineConfig.itemSpacing, "物品垂直间距");
        add(slotMachineConfig.itemVisibleCount, "可见物品数量");
        add(slotMachineConfig.itemSlotSize, "转盘槽位大小");
        add(slotMachineConfig.itemMinAlpha, "物品最小透明度");
        add(slotMachineConfig.itemMaxAlpha, "物品最大透明度");
        add(slotMachineConfig.decelZone, "滚动减速区距离");
        add(slotMachineConfig.decelZoneMinSpeed, "减速阶段最低速度倍率");
        add(slotMachineConfig.decelZoneVarianceMin, "随机减速倍率范围最低");
        add(slotMachineConfig.decelZoneVarianceMax, "随机减速倍率范围最高");
        add(slotMachineConfig.speedVarianceMin, "随机速度倍率范围最低");
        add(slotMachineConfig.speedVarianceMax, "随机速度倍率范围最高");
        add(slotMachineConfig.minSpeedVarianceMin, "随机最小速度倍率范围最低");
        add(slotMachineConfig.minSpeedVarianceMax, "随机最小速度倍率范围最高");
        add(slotMachineConfig.exitSpeed, "退出动画每tick进度推进量");
        add(slotMachineConfig.exitMaxScale, "退出动画最大放大倍率");
        add(slotMachineConfig.passSoundEffects, "拨片音效");
        add(slotMachineConfig.passSoundEffectsOptimize, "拨片音效优化");
    }

    public static void addI18nSoundEventText(String zhName, Supplier<? extends SoundEvent> supplier) {
        if (DatagenModLoader.isRunningDataGen()) {
            SOUND_EVENT.put(supplier, zhName);
        }
    }
}
