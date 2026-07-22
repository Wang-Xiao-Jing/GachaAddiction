package xiaojin.gachaaddiction.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.GachaAddictionConfig;
import xiaojin.gachaaddiction.client.gui.screen.SlotMachineScreen;

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
        add(GachaAddictionConfig.CLIENT.mergeItem, "合并抽奖物品");
        add(GachaAddictionConfig.CLIENT.rewardSoundEffects, "奖品音效");

        add(GachaAddiction.MODID + ".configuration.gachaa_screen.slot_machine", "老虎机");
        add(GachaAddiction.MODID + ".configuration.gachaa_screen.slot_machine.item", "物品");
        add(GachaAddiction.MODID + ".configuration.gachaa_screen.slot_machine.decel_zone", "减速");
        add(GachaAddiction.MODID + ".configuration.gachaa_screen.slot_machine.speed", "速度");
        add(GachaAddictionConfig.CLIENT.slotMachineItemsPerSecond, "每秒经过的物品数量");
        add(GachaAddictionConfig.CLIENT.slotMachineItemSpacing, "物品垂直间距");
        add(GachaAddictionConfig.CLIENT.slotMachineVisibleCount, "可见物品数量");
        add(GachaAddictionConfig.CLIENT.slotMachineSlotSize, "转盘槽位大小");
        add(GachaAddictionConfig.CLIENT.slotMachineMinAlpha, "物品最小透明度");
        add(GachaAddictionConfig.CLIENT.slotMachineMaxAlpha, "物品最大透明度");
        add(GachaAddictionConfig.CLIENT.slotMachineDecelZone, "滚动减速区距离");
        add(GachaAddictionConfig.CLIENT.slotMachineMinSpeed, "减速阶段最低速度倍率");
        add(GachaAddictionConfig.CLIENT.slotMachineDecelZoneVarianceMin, "随机减速倍率范围最低");
        add(GachaAddictionConfig.CLIENT.slotMachineDecelZoneVarianceMax, "随机减速倍率范围最高");
        add(GachaAddictionConfig.CLIENT.slotMachineSpeedVarianceMin, "随机速度倍率范围最低");
        add(GachaAddictionConfig.CLIENT.slotMachineSpeedVarianceMax, "随机速度倍率范围最高");
        add(GachaAddictionConfig.CLIENT.slotMachineMinSpeedVarianceMin, "随机最小速度倍率范围最低");
        add(GachaAddictionConfig.CLIENT.slotMachineMinSpeedVarianceMax, "随机最小速度倍率范围最高");
        add(GachaAddictionConfig.CLIENT.slotMachineExitSpeed, "退出动画每tick进度推进量");
        add(GachaAddictionConfig.CLIENT.slotMachineExitMaxScale, "退出动画最大放大倍率");
    }

    public static void addI18nSoundEventText(String zhName, Supplier<? extends SoundEvent> supplier) {
        if (DatagenModLoader.isRunningDataGen()) {
            SOUND_EVENT.put(supplier, zhName);
        }
    }
}
