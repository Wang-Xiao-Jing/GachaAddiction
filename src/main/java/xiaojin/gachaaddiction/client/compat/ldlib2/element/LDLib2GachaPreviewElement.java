package xiaojin.gachaaddiction.client.compat.ldlib2.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.api.client.ReelHost;
import xiaojin.gachaaddiction.client.gui.screen.SlotMachineScreen;
import xiaojin.gachaaddiction.client.gui.widget.VerticalReelWidget;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.config.entry.BasicConfigEntry;

import java.util.ArrayList;
import java.util.List;

public class LDLib2GachaPreviewElement extends UIElement implements ReelHost {
    private static final List<Item> SAMPLE_ITEMS = List.of(Items.STONE, Items.GLASS, Items.ENCHANTED_BOOK);
    private static final int SAMPLE_COUNT = 1;
    private static final int SAMPLE_WEIGHT = 1;
    private static final int REPLAY_DELAY_TICKS = 30;
    private static final int REEL_WIDTH = 32;
    private static final int MINIMUM_HEIGHT = 96;
    private static final String CONFIG_CATEGORY = "slot_machine";

    private final List<VerticalReelWidget> reels = new ArrayList<>();
    private List<?> configSnapshot = List.of();
    private int completedTicks;

    public LDLib2GachaPreviewElement() {
        layout(layout -> layout.widthPercent(100).flex(1).minHeight(MINIMUM_HEIGHT));
        replay();
    }

    private static List<?> currentConfig() {
        return GachaAddictionConfig.clientEntries().stream()
                .filter(entry -> entry.path().contains(CONFIG_CATEGORY))
                .map(BasicConfigEntry::get).toList();
    }

    public void replay() {
        configSnapshot = currentConfig();
        completedTicks = 0;
        reels.clear();
        var entries = SAMPLE_ITEMS.stream()
                .map(item -> new ItemStackEntry(new ItemStack(item), SAMPLE_WEIGHT)).toList();
        var random = RandomSource.create();
        for (var item : SAMPLE_ITEMS) {
            reels.add(SlotMachineScreen.createReelWidget(this, new RewardData(new ItemStack(item), SAMPLE_COUNT),
                    entries, GachaAddictionConfig.CLIENT.slotMachineConfig, random));
        }
    }

    @Override
    public void screenTick() {
        super.screenTick();
        if (!configSnapshot.equals(currentConfig())) {
            replay();
        } else if (reels.stream().allMatch(VerticalReelWidget::isComplete)
                && ++completedTicks >= REPLAY_DELAY_TICKS) {
            replay();
        }
    }

    @Override
    public void drawBackgroundAdditional(@NotNull GUIContext context) {
        super.drawBackgroundAdditional(context);
        float spacing = Math.min(REEL_WIDTH, getContentWidth() / reels.size());
        float startX = getContentX() + (getContentWidth() - spacing * (reels.size() - 1)) / 2;
        int centerY = Math.round(getContentY() + getContentHeight() / 2);
        for (int i = 0; i < reels.size(); i++) {
            var reel = reels.get(i);
            reel.setX(Math.round(startX + spacing * i));
            reel.setY(centerY);
            reel.render(context.graphics, context.mouseX, context.mouseY, context.partialTick);
        }
    }

    @Override
    public boolean tryClaimPassSound() {
        return false;
    }

    @Override
    public int getCurrentRenderHighestNewCompleteLevel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean tryClaimCompleteSound() {
        return false;
    }

    @Override
    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }
}
