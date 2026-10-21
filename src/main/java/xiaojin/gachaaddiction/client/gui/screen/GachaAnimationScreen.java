package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.render.GachaAnimationRenderer;
import xiaojin.gachaaddiction.client.render.GachaReelFade;
import xiaojin.gachaaddiction.client.render.GachaSceneRenderer;
import xiaojin.gachaaddiction.client.render.GachaChestAnimation;
import xiaojin.gachaaddiction.client.render.ModernGachaEffect;
import xiaojin.gachaaddiction.network.cts.FinishGachaItemDrawPayload;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class GachaAnimationScreen extends ModernGachaScreen {
    private static final long SETTLE_MILLIS = 2200;
    private static final int NEUTRAL_CHEST_LIGHT = 0xD0D0D0;
    private static final long SPIN_MILLIS = 2000;
    private static final float CHEST_SWIPE_FRACTION = 0.25F;
    private static final float MILLIS_PER_SECOND = 1000.0F;
    private static final float MAXIMUM_FRAME_SECONDS = 0.1F;
    private static final float STRIP_SPEED = 8.0F;
    private static final float WHEEL_SPEED = 5.0F;
    private static final float SLOT_SPEED = 12.0F;
    private static final float MINIMUM_CHARGE = 0.2F;
    private static final float CHARGE_SPEED_FACTOR = 0.75F;
    private static final float BASE_SPEED_FACTOR = 0.5F;
    private static final float PREVIEW_LABEL_FRACTION = 0.89F;
    private static final int HINT_MARGIN = 14;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final float CHEST_REFLECTION_BASE = 0.72F;
    private static final float FULL_TURN = (float) (Math.PI * 2.0);
    private final GachaType animation;
    private final GachaSceneRenderer scene;
    private @Nullable UUID session;
    private List<ItemStack> icons = List.of();
    private final ModernGachaEffect backdrop = new ModernGachaEffect();
    private final GachaReelFade reelFade = new GachaReelFade();
    private FrontStage frontStage = FrontStage.READY;
    private long startedAt = Util.getMillis();
    private long lastFrameAt = startedAt;
    private long settlingAt;
    private long expiresAt = Long.MAX_VALUE;
    private float motion;
    private float charge;
    private float lastDragAngle;
    private double chestDragStartY;
    private boolean chestStarted;
    private int chestLightColor = NEUTRAL_CHEST_LIGHT;
    private boolean loaded;
    private boolean dragging;
    private boolean requestSent;
    private boolean spaceHeld;

    public GachaAnimationScreen(@Nullable Screen parent, List<@Nullable ResourceLocation> tables,
                                List<ItemStackEntry> entries, GachaType animation) {
        super(parent, tables, entries);
        this.animation = animation;
        scene = new GachaSceneRenderer(parent);
    }

    public GachaAnimationScreen(@Nullable Screen parent, GachaType animation, UUID session,
                                List<ItemStack> icons, long lifetimeMillis) {
        this(parent, List.of(), List.of(), animation);
        this.session = session;
        this.icons = icons.stream().map(ItemStack::copy).toList();
        this.loaded = true;
        this.expiresAt = Util.getMillis() + Math.max(0, lifetimeMillis);
        if (animation == GachaTypes.TOTEM) {
            frontStage = FrontStage.RUNNING;
        }
    }

    @Override
    public void updateRewards(NonNullList<ItemStack> items) {
        if (loaded) {
            return;
        }
        super.updateRewards(items);
        updateChestLightColor();
        icons = rewards.stream().map(RewardData::getItemStack).map(ItemStack::copy).toList();
        loaded = true;
        startedAt = Util.getMillis();
        lastFrameAt = startedAt;
        if (animation == GachaTypes.TOTEM) {
            frontStage = FrontStage.RUNNING;
        }
    }

    public boolean matchesSession(UUID id) {
        return id.equals(session);
    }

    @Override
    public ItemStack getTriggerItem() {
        ItemStack trigger = super.getTriggerItem();
        return trigger.isEmpty() ? scene.getTriggerItem() : trigger;
    }

    @Override
    public void preparePresentationReplacement() {
        scene.rememberForReplacement(getTriggerItem());
    }

    public void receiveResult(boolean success, List<ItemStack> items) {
        if (session == null || !requestSent || frontStage != FrontStage.WAITING) {
            return;
        }
        if (!success) {
            frontStage = FrontStage.FAILED;
            backdrop.close();
            completePresentation();
            return;
        }
        NonNullList<ItemStack> result = NonNullList.create();
        items.forEach(item -> result.add(item.copy()));
        super.updateRewards(result, true);
        updateChestLightColor();
        frontStage = FrontStage.SUMMARY;
        if (animation != GachaTypes.CHEST) {
            backdrop.close();
        }
    }

    private void updateChestLightColor() {
        chestLightColor = rewards.stream().max(Comparator.comparingInt(RewardData::getRarityLevel))
                .map(RewardData::getRarityColor).orElse(NEUTRAL_CHEST_LIGHT);
    }

    private float chestBoxDrop() {
        return frontStage == FrontStage.RUNNING && chestStarted ? GachaChestAnimation.drop(Util.getMillis() - startedAt) : 0.0F;
    }

    private float chestOpening() {
        return frontStage == FrontStage.RUNNING && chestStarted ? GachaChestAnimation.opening(Util.getMillis() - startedAt) : 0.0F;
    }

    private int frontColor() {
        return animation == GachaTypes.CHEST ? chestLightColor : GachaAnimationRenderer.accentColor();
    }

    private void renderChestBackground(GuiGraphics graphics) {
        if (!backdrop.renderBackdrop(graphics, chestLightColor,
                (Util.getMillis() - startedAt) / MILLIS_PER_SECOND)) {
            graphics.fill(0, 0, width, height, GachaAnimationRenderer.backgroundColor());
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (frontStage == FrontStage.SUMMARY && animation != GachaTypes.CHEST) {
            super.renderBackground(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (animation == GachaTypes.CHEST && frontStage == FrontStage.RUNNING && !chestStarted) {
            startedAt = Util.getMillis();
            chestStarted = true;
        }
        updateFront();
        if (frontStage == FrontStage.SUMMARY) {
            if (animation == GachaTypes.CHEST) {
                renderChestBackground(graphics);
                renderSourceTitle(graphics);
                renderSummaryItems(graphics, mouseX, mouseY, partialTick);
                return;
            }
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        long now = Util.getMillis();
        float seconds = (now - startedAt) / MILLIS_PER_SECOND;
        float drop = animation == GachaTypes.CHEST ? chestBoxDrop() : 0.0F;
        if (frontStage == FrontStage.FAILED) {
            drop = 0.0F;
        }
        if (animation == GachaTypes.CHEST && frontStage == FrontStage.WAITING) {
            renderChestBackground(graphics);
        } else if (frontStage == FrontStage.WAITING || frontStage == FrontStage.FAILED
                || !backdrop.renderBackdrop(graphics, frontColor(), seconds)) {
            graphics.fill(0, 0, width, height, GachaAnimationRenderer.backgroundColor());
        }
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0.0F, height * drop, 0.0F);
            GachaAnimationRenderer.Layout layout = GachaAnimationRenderer.layout(width, height);
            if (loaded && drop < 1.0F && frontStage != FrontStage.FAILED && frontStage != FrontStage.WAITING) {
                GachaAnimationRenderer.render(graphics, animation, layout, icons, motion, seconds,
                        animation == GachaTypes.CHEST ? chestOpening() : charge,
                        frontStage != FrontStage.READY, scene, getTriggerItem(), reelFade, frontColor());
            }
        } finally {
            try {
                graphics.flush();
            } finally {
                graphics.pose().popPose();
            }
        }
        if (animation == GachaTypes.CHEST && loaded && drop < 1.0F
                && frontStage != FrontStage.FAILED && frontStage != FrontStage.WAITING) {
            GachaAnimationRenderer.Layout layout = GachaAnimationRenderer.layout(width, height);
            float offset = height * drop;
            scene.renderReflection(graphics, layout.y(CHEST_REFLECTION_BASE) + offset, 1.0F - drop, () -> {
                graphics.pose().pushPose();
                try {
                    graphics.pose().translate(0.0F, offset, 0.0F);
                    scene.renderChest(graphics, layout, seconds, chestOpening(),
                            getTriggerItem(), frontColor());
                } finally {
                    graphics.pose().popPose();
                }
            });
        }
        renderSourceTitle(graphics);
        String hint = !loaded || frontStage == FrontStage.WAITING ? TranslationKeys.GUI_ANIMATION_WAITING
                : frontStage == FrontStage.FAILED ? TranslationKeys.GUI_ANIMATION_FAILED
                : isForceWatch() ? forcedHintKey() : hintKey();
        graphics.drawCenteredString(font, Component.translatable(hint), width / 2,
                height - HINT_MARGIN - font.lineHeight, TEXT_COLOR);
        if (session != null && frontStage != FrontStage.FAILED && frontStage != FrontStage.WAITING) {
            graphics.drawCenteredString(font, Component.translatable(TranslationKeys.GUI_ANIMATION_PREVIEW),
                    width / 2, (int) (height * PREVIEW_LABEL_FRACTION), frontColor());
        }
        if (animation == GachaTypes.CHEST && session == null && loaded && frontStage == FrontStage.RUNNING
                && Util.getMillis() - startedAt >= GachaChestAnimation.ACTIVATE_MILLIS) {
            renderSummaryPreview(graphics, partialTick, drop, false);
        } else {
            resetSummaryPreview();
        }
    }

    private String hintKey() {
        if (frontStage == FrontStage.RUNNING || frontStage == FrontStage.SETTLING) {
            return TranslationKeys.GUI_ANIMATION_RUNNING;
        }
        if (animation == GachaTypes.CHEST) {
            return TranslationKeys.GUI_ANIMATION_CHEST;
        }
        if (animation == GachaTypes.CSGO) {
            return TranslationKeys.GUI_ANIMATION_CSGO;
        }
        if (animation == GachaTypes.WHEEL) {
            return TranslationKeys.GUI_ANIMATION_WHEEL;
        }
        if (animation == GachaTypes.INTERACTIVE_SLOT_MACHINE) {
            return TranslationKeys.GUI_ANIMATION_SLOT;
        }
        return TranslationKeys.GUI_ANIMATION_TOTEM;
    }

    private String forcedHintKey() {
        return animation == GachaTypes.CHEST && frontStage == FrontStage.READY ? TranslationKeys.GUI_ANIMATION_CHEST_FORCED
                : animation == GachaTypes.WHEEL && frontStage == FrontStage.READY ? TranslationKeys.GUI_ANIMATION_WHEEL_FORCED
                : frontStage == FrontStage.READY ? TranslationKeys.GUI_ANIMATION_CLICK_FORCED : TranslationKeys.GUI_ANIMATION_FORCED;
    }

    private void updateFront() {
        long now = Util.getMillis();
        float elapsed = Math.clamp((now - lastFrameAt) / MILLIS_PER_SECOND, 0.0F, MAXIMUM_FRAME_SECONDS);
        lastFrameAt = now;
        if (!loaded || frontStage == FrontStage.SUMMARY || frontStage == FrontStage.FAILED) {
            return;
        }
        if (session != null && !requestSent && now >= expiresAt) {
            PacketDistributor.sendToServer(new FinishGachaItemDrawPayload(session, true));
            frontStage = FrontStage.FAILED;
            completePresentation();
            backdrop.close();
            return;
        }
        if (animation == GachaTypes.CHEST) {
            if (frontStage == FrontStage.RUNNING && chestStarted && now - startedAt >= GachaChestAnimation.TOTAL_MILLIS) {
                finishPresentation();
            }
            return;
        }
        if (frontStage == FrontStage.RUNNING || frontStage == FrontStage.SETTLING) {
            float speed = animation == GachaTypes.CSGO ? STRIP_SPEED
                    : animation == GachaTypes.WHEEL ? WHEEL_SPEED : SLOT_SPEED;
            float braking = 1.0F - settlingProgress(now);
            motion += elapsed * speed * (BASE_SPEED_FACTOR + charge * CHARGE_SPEED_FACTOR) * braking * braking;
        }
        if (animation == GachaTypes.TOTEM && GachaSceneRenderer.hasTotemReachedClosest((now - startedAt) / MILLIS_PER_SECOND)) {
            finishPresentation();
        } else if (frontStage == FrontStage.RUNNING && now - startedAt >= SPIN_MILLIS) {
            brake();
        } else if (frontStage == FrontStage.SETTLING && now - settlingAt >= settlingMillis()) {
            finishPresentation();
        }
    }

    private float settlingProgress(long now) {
        return frontStage == FrontStage.SETTLING
                ? Math.clamp((float) (now - settlingAt) / settlingMillis(), 0.0F, 1.0F) : 0.0F;
    }

    private long settlingMillis() {
        return SETTLE_MILLIS;
    }

    private void brake() {
        frontStage = FrontStage.SETTLING;
        settlingAt = Util.getMillis();
        dragging = false;
    }

    private void finishPresentation() {
        if (frontStage == FrontStage.SUMMARY || frontStage == FrontStage.WAITING
                || frontStage == FrontStage.FAILED) {
            return;
        }
        if (animation == GachaTypes.CHEST) {
            scene.close();
        } else {
            backdrop.close();
        }
        reelFade.close();
        dragging = false;
        if (session == null) {
            beginSummary();
            frontStage = FrontStage.SUMMARY;
        } else if (!requestSent) {
            requestSent = true;
            frontStage = FrontStage.WAITING;
            PacketDistributor.sendToServer(new FinishGachaItemDrawPayload(session, false));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (frontStage == FrontStage.SUMMARY) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !loaded || frontStage == FrontStage.WAITING
                || frontStage == FrontStage.FAILED) {
            return false;
        }
        if (frontStage == FrontStage.SETTLING || frontStage == FrontStage.RUNNING) {
            if (!isForceWatch() && animation != GachaTypes.TOTEM && animation != GachaTypes.CHEST) {
                finishPresentation();
            }
            return true;
        }
        GachaAnimationRenderer.Layout layout = GachaAnimationRenderer.layout(width, height);
        if (animation == GachaTypes.CHEST && GachaAnimationRenderer.overChest(layout, mouseX, mouseY - height * chestBoxDrop())) {
            dragging = true;
            chestDragStartY = mouseY;
        } else if (animation == GachaTypes.WHEEL && GachaAnimationRenderer.overWheel(layout, mouseX, mouseY)) {
            dragging = true;
            charge = 0.0F;
            lastDragAngle = GachaAnimationRenderer.wheelAngle(layout, mouseX, mouseY);
        } else if (animation == GachaTypes.CSGO || animation == GachaTypes.INTERACTIVE_SLOT_MACHINE) {
            startSpinning();
        }
        return true;
    }

    private void startSpinning() {
        frontStage = FrontStage.RUNNING;
        startedAt = Util.getMillis();
        lastFrameAt = startedAt;
        dragging = false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (frontStage == FrontStage.SUMMARY) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        if (!dragging || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        GachaAnimationRenderer.Layout layout = GachaAnimationRenderer.layout(width, height);
        if (animation == GachaTypes.CHEST) {
            if (chestDragStartY - mouseY >= Math.max(1, height * CHEST_SWIPE_FRACTION)) {
                startSpinning();
                chestStarted = false;
            }
        } else if (animation == GachaTypes.WHEEL) {
            float angle = GachaAnimationRenderer.wheelAngle(layout, mouseX, mouseY);
            float delta = angle - lastDragAngle;
            delta -= Math.round(delta / FULL_TURN) * FULL_TURN;
            motion += delta;
            charge = Math.clamp(charge + Math.abs(delta) / FULL_TURN, 0.0F, 1.0F);
            lastDragAngle = angle;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (frontStage == FrontStage.SUMMARY) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        if (dragging && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = false;
            if (animation == GachaTypes.WHEEL && charge >= MINIMUM_CHARGE) {
                startSpinning();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE && spaceHeld) {
            return true;
        }
        if (frontStage == FrontStage.SUMMARY) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            if (!spaceHeld && loaded && !isForceWatch()) {
                spaceHeld = true;
                finishPresentation();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            spaceHeld = false;
        }
        return frontStage == FrontStage.SUMMARY && super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return frontStage == FrontStage.SUMMARY && super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void removed() {
        backdrop.close();
        scene.close();
        reelFade.close();
        if (session != null && frontStage != FrontStage.SUMMARY && minecraft != null
                && minecraft.getConnection() != null) {
            PacketDistributor.sendToServer(new FinishGachaItemDrawPayload(session, true));
        }
        super.removed();
    }

    private enum FrontStage {
        READY, RUNNING, SETTLING, WAITING, SUMMARY, FAILED
    }
}
