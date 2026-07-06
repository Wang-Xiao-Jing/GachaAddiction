package xiaojin.gachaaddiction.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.yanbwe.raritycore.api.RarityCoreAPI;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.util.DisplayEntry;

import java.util.ArrayList;
import java.util.List;

public class GachaScreen extends Screen {
    public static final int DISPLAY_QUANTITY = 5;
    private final Screen originalScreen;
    private final ResourceKey<LootTable> lootTableResourceKey;
    private final List<DisplayEntry> entries;
    private final List<ReelWidget> reelWidgets = new ArrayList<>();
    private boolean isRemoved;

    public GachaScreen(Screen originalScreen, ResourceKey<LootTable> lootTableResourceKey, List<DisplayEntry> entries) {
        super(Component.empty());
        this.originalScreen = originalScreen;
        this.lootTableResourceKey = lootTableResourceKey;
        this.entries = entries;
    }

    private static int getRarityColor(ItemStack itemStack) {
        if (GachaAddiction.RARITYCORE_LOADED) {
            return RarityCoreAPI.getColor(RarityCoreAPI.getRarity(itemStack));
        }

        Rarity rarity = itemStack.getRarity();
        Integer color = rarity.color().getColor();
        if (color != null) {
            return color;
        }
        return ChatFormatting.GRAY.getColor();
    }

    private static int getRarityLevel(ItemStack itemStack) {
        if (GachaAddiction.RARITYCORE_LOADED) {
            return RarityCoreAPI.getRarity(itemStack);
        }
        return itemStack.getRarity().ordinal();
    }

    public void initializeContents() {
        if (!(originalScreen instanceof MenuAccess<?> menuAccess)) {
            return;
        }
        NonNullList<ItemStack> items = menuAccess.getMenu().getItems();
        List<ItemStack> newItems = items.stream().filter(itemStack1 -> !itemStack1.isEmpty()).toList();
        for (var v : reelWidgets) {
            removeWidget(v);
        }
        reelWidgets.clear();
        RandomSource random = RandomSource.create();
        for (ItemStack itemStack : newItems) {
            int rarityLevel = getRarityLevel(itemStack);
            List<ItemStack> decoys = DisplayEntry.generateGachaResult(entries, 20 + random.nextInt(rarityLevel * 2, rarityLevel * DISPLAY_QUANTITY), random);
            int resultIndex = random.nextInt(20, decoys.size()) - DISPLAY_QUANTITY;
            decoys.add(resultIndex, itemStack.getItem().getDefaultInstance());
            reelWidgets.add(new ReelWidget(itemStack, decoys, resultIndex));
        }
        initReels();
    }

    @Override
    protected void init() {
        super.init();
        initReels();
    }

    private void initReels() {
        int reelLength = reelWidgets.size();
        int i1 = Math.min(13, width / (reelLength + 1));
        for (int i = 0; i < reelLength; i++) {
            ReelWidget reelWidget = reelWidgets.get(i);
            int x = i1 + i * (16 + 4);
            reelWidget.setPosition(x, height / 2);
            reelWidget.visible = reelWidget.getX() <= width && reelWidget.getX() >= 0 && reelWidget.getY() <= height && reelWidget.getY() >= 0;
            addRenderableWidget(reelWidget);
        }
    }

    @Override
    public void removed() {
        super.removed();
    }

    public Screen getOriginalScreen() {
        return originalScreen;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTransparentBackground(guiGraphics);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static class ReelWidget extends AbstractWidget {
        private final ItemStack result;
        private final List<ItemStack> decoys;
        /**
         * 结果索引
         */
        private int resultIndex;

        private float offset;
        private boolean complete;

        public ReelWidget(ItemStack result, List<ItemStack> decoys, int resultIndex) {
            super(0, 0, 16, 0, Component.empty());
            this.result = result;
            this.decoys = decoys;
            this.resultIndex = resultIndex;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (-offset < resultIndex) {
                offset -= partialTick / 3;
            }

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(getX(), getY(), 0);
            for (int i = 0; i < decoys.size(); i++) {
                ItemStack itemStack = decoys.get(i);
                float value = i + offset - (DISPLAY_QUANTITY / 2 + 1);
                float j = Math.clamp(value, -DISPLAY_QUANTITY, DISPLAY_QUANTITY);
                float sczle = 1 - Math.abs(j) / (DISPLAY_QUANTITY);
                if (sczle <= 0) {
                    continue;
                }
                poseStack.pushPose();

                RenderSystem.enableBlend();
                guiGraphics.setColor(1, 1, 1, sczle);

                poseStack.translate(getX(), 16 * j, 0);
                poseStack.scale(sczle, sczle, sczle);
                guiGraphics.renderFakeItem(itemStack, -8, -8);
                guiGraphics.setColor(1, 1, 1, 1);
                RenderSystem.disableBlend();

                poseStack.popPose();
            }
            poseStack.popPose();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        public void stopped() {
            complete = true;
            offset = resultIndex;
        }

        public boolean isComplete() {
            return complete;
        }
    }
}
