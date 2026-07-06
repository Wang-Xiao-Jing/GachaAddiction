package xiaojin.gachaaddiction.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.storage.loot.LootTable;
import org.lwjgl.glfw.GLFW;
import org.yanbwe.raritycore.api.RarityCoreAPI;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.util.DisplayEntry;

import java.util.ArrayList;
import java.util.List;

public class GachaScreen extends Screen {
    private final Screen originalScreen;
    private final ResourceKey<LootTable> lootTableResourceKey;
    private final List<DisplayEntry> entries;
    private final List<ReelWidget> reelWidgets = new ArrayList<>();
    private boolean isRemoved;
    private int currentPage;
    private int pageSize;
    private float slideProgress = 1f;
    private static final float SLIDE_SPEED = 2f;


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
            List<ItemStack> decoys = DisplayEntry.generateGachaResult(entries, 20 + random.nextInt(rarityLevel * 2, rarityLevel * ReelConfig.VISIBLE_COUNT), random);
            int resultIndex = random.nextInt(20, decoys.size()) - ReelConfig.VISIBLE_COUNT;
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
        initilizePage(0);
    }

    private void initilizePage(int page) {
        children().removeIf(w -> w instanceof ReelWidget);
        renderables.removeIf(w -> w instanceof ReelWidget);

        pageSize = Math.max(1, width / 3 * 2 / 32);
        int from = page * pageSize;
        int to = Math.min(from + pageSize, reelWidgets.size());

        if (from >= reelWidgets.size()) {
//            this.isRemoved = true;
//            onClose();
//            Minecraft.getInstance().setScreen(null);
            return;
        }

        List<ReelWidget> pageWidgets = reelWidgets.subList(from, to);
        int totalWidth = pageWidgets.size() * 32;
        int startX = (width - totalWidth) / 2;

        for (int i = 0; i < pageWidgets.size(); i++) {
            ReelWidget w = pageWidgets.get(i);
            w.setBaseX(startX + i * 32);
            w.setY(height / 2);
            w.visible = true;
            addRenderableWidget(w);
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

    @Override
    public void tick() {
        if (slideProgress >= 1f) return;
        slideProgress += (1f / 20f) * SLIDE_SPEED;
        float eased = slideProgress < 0.5f ? 2f * slideProgress * slideProgress : 1f - (float) Math.pow(-2f * slideProgress + 2f, 2) / 2f;
        float offset = -eased * width;
        int from = currentPage * pageSize;
        int to = Math.min(from + pageSize, reelWidgets.size());
        for (int i = from; i < to; i++) {
            ReelWidget w = reelWidgets.get(i);
            w.setX((int) (w.getBaseX() + offset));
        }
        if (slideProgress >= 1f) {
            finishSlide();
        }
    }

    private void finishSlide() {
        children().removeIf(w -> w instanceof ReelWidget);
        renderables.removeIf(w -> w instanceof ReelWidget);
        initilizePage(currentPage);
        slideProgress = 1f;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            boolean allDone = reelWidgets.stream().skip(currentPage * pageSize).limit(pageSize).allMatch(ReelWidget::isComplete);
            if (allDone) {
                int nextFrom = (currentPage + 1) * pageSize;
                if (nextFrom >= reelWidgets.size()) {
                    this.isRemoved = true;
                    onClose();
                    Minecraft.getInstance().setScreen(null);
                } else {
                    currentPage++;
                    slideProgress = 0f;
                }
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.isRemoved = true;
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static class ReelConfig {
        public static final float SPEED = 0.5f;
        public static final float ITEM_SPACING = 18f;
        public static final int VISIBLE_COUNT = 5;
        public static final Easing EASING = Easing.APPROACH;
        public static final float MIN_SCALE = 1.0f;
        public static final float MAX_SCALE = 1.2f;
        public static final float MIN_ALPHA = 0.0f;
        public static final float MAX_ALPHA = 1.0f;

        public enum Easing {
            LINEAR,
            EASE_OUT_CUBIC,
            EASE_IN_OUT_QUAD,
            APPROACH;

            public float apply(float t) {
                return switch (this) {
                    case LINEAR -> t;
                    case EASE_OUT_CUBIC -> {
                        float p = 1f - t;
                        yield 1f - p * p * p;
                    }
                    case EASE_IN_OUT_QUAD -> {
                        if (t < 0.5f) yield 2f * t * t;
                        float p = -2f * t + 2f;
                        yield 1f - p * p / 2f;
                    }
                    case APPROACH -> {
                        if (t < 0.65f) yield t;
                        float local = (t - 0.65f) / 0.35f;
                        float p = 1f - local;
                        yield 0.65f + 0.35f * (local * (1f + local * (1f - local)));
                    }
                };
            }

            public float getSpeedFactor(float t) {
                if (this != APPROACH || t < 0.65f) return 1f;
                float local = (t - 0.65f) / 0.35f;
                return Math.max(0.1f, 1f - local * local);
            }
        }
    }

    public static class ReelWidget extends AbstractWidget {
        private final ItemStack result;
        private final List<ItemStack> decoys;
        private final int resultIndex;

        private float visualOffset;
        private boolean complete;
        private float progress;
        private final float randomOffset;
        private int baseX;
        private int lastPassedIndex;
        private boolean soundPlayed;

        public ReelWidget(ItemStack result, List<ItemStack> decoys, int resultIndex) {
            super(0, 0, 16, 0, Component.empty());
            this.result = result;
            this.decoys = decoys;
            this.resultIndex = resultIndex;
            this.randomOffset = 0;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            float target = -(resultIndex + randomOffset) + (ReelConfig.VISIBLE_COUNT - 1) / 2f;

            if (!complete) {
                float speedMul = ReelConfig.EASING.getSpeedFactor(progress);
                progress += (partialTick * ReelConfig.SPEED * speedMul) / decoys.size();
                if (progress >= 1f) {
                    progress = Math.min(progress, 1f);
                    complete = true;
                }
            }

            float eased = ReelConfig.EASING.apply(progress);
            visualOffset = target * eased;

            int passedIndex = (int) Math.floor(-visualOffset);
            if (passedIndex != lastPassedIndex) {
                lastPassedIndex = passedIndex;
                if (!complete) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
            }

            if (complete && !soundPlayed) {
                soundPlayed = true;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F));
            }

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(getX(), getY(), 0);

            poseStack.pushPose();
            poseStack.translate(0, 0, 0);
            RenderSystem.enableBlend();
            for (int i = 0; i < decoys.size(); i++) {
                float value = (i + visualOffset) - ((ReelConfig.VISIBLE_COUNT - 1) / 2f);
                float clamped = Math.clamp(value, -ReelConfig.VISIBLE_COUNT, ReelConfig.VISIBLE_COUNT);
                float normalized = Math.abs(clamped) / ReelConfig.VISIBLE_COUNT;
                if (normalized >= 1.1f) continue;

                float scale = 1.0f + 0.25f * (1.0f - Math.min(normalized / 0.20f, 1.0f));
                float alpha = ReelConfig.MAX_ALPHA - (ReelConfig.MAX_ALPHA - ReelConfig.MIN_ALPHA) * normalized;
                if (alpha <= 0f || scale <= 0f) continue;

                poseStack.pushPose();
                guiGraphics.setColor(1f, 1f, 1f, alpha);

                poseStack.translate(0f, ReelConfig.ITEM_SPACING * clamped, 0f);
                poseStack.scale(scale, scale, scale);
                guiGraphics.renderFakeItem(decoys.get(i), -8, -8);

                guiGraphics.setColor(1f, 1f, 1f, 1f);
                poseStack.popPose();
            }
            RenderSystem.disableBlend();
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0, 0, 10000);
            poseStack.scale(1.25f, 1.25f, 1.25f);
            guiGraphics.blitSprite(ResourceLocation.parse("hud/hotbar_selection"), -24 / 2, -24 / 2, 24, 23);
            poseStack.popPose();
            poseStack.popPose();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }

        public void skipToEnd() {
            visualOffset = -(resultIndex + randomOffset) + (ReelConfig.VISIBLE_COUNT - 1) / 2f;
            complete = true;
        }

        public void setBaseX(int x) {
            this.baseX = x;
            setX(x);
        }

        public int getBaseX() {
            return baseX;
        }

        public boolean isComplete() {
            return complete;
        }

        public ItemStack getResult() {
            return result;
        }
    }
}
