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
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.api.GachaSecondaryType;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.render.GachaAnimationRenderer;
import xiaojin.gachaaddiction.client.render.GachaSceneRenderer;
import xiaojin.gachaaddiction.client.render.GachaChestAnimation;
import xiaojin.gachaaddiction.client.render.GachaTotemImpact;
import xiaojin.gachaaddiction.client.render.ModernGachaEffect;
import xiaojin.gachaaddiction.network.cts.FinishGachaItemDrawPayload;
import xiaojin.gachaaddiction.network.stc.GachaPresentationPayload;
import xiaojin.gachaaddiction.registry.GachaTypes;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class GachaSourceInteractionScreen extends GachaSummaryScreen {
    private static final long FOCUS_MILLIS = 240;
    private static final long FALL_MILLIS = 350;
    private static final long REFLOW_MILLIS = 300;
    private static final float MILLIS_PER_SECOND = 1000.0F;
    private static final float GRID_WIDTH = 0.8F;
    private static final float ROW_HEIGHT = 0.32F;
    private static final float MULTIPLE_SCALE = 0.55F;
    private static final float SCROLL_STEP = 0.75F;
    private static final int SCROLL_TRACK_COLOR = 0x50666666;
    private static final int SCROLL_THUMB_COLOR = 0xB0FFFFFF;
    private static final int SCROLL_TRACK_HEIGHT = 2;
    private static final float CELL_CONTENT = 0.72F;
    private static final float OTHER_SHRINK = 0.5F;
    private static final float STRAIGHTEN_START = 0.7F;
    private static final float FALL_DISTANCE = 1.3F;
    private static final float ROUND_WIDTH = 0.62F;
    private static final float ROUND_HEIGHT = 0.6F;
    private static final int NEUTRAL_LIGHT = 0xD0D0D0;
    private static final int FOOTER_MARGIN = 24;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final float ITEM_REFLECTION_BASE = 0.52F;
    private static final float CHEST_REFLECTION_BASE = 0.36F;
    private static final long REFLECTION_FADE_MILLIS = 240;
    private final GachaType animation;
    private final GachaSceneRenderer scene;
    private final ModernGachaEffect backdrop = new ModernGachaEffect();
    private final GachaTotemImpact totemImpact = new GachaTotemImpact();
    private @Nullable UUID session;
    private @Nullable Component sourceTitle;
    private GachaSecondaryType secondary = GachaSecondaryType.NONE;
    private List<List<ItemStack>> rounds = List.of();
    private List<ItemStack> aggregate = List.of();
    private boolean[] completed = new boolean[1];
    private List<Integer> remainingObjects = List.of(0);
    private int objectCount = 1;
    private int selected;
    private float scrollOffset;
    private int lightColor = NEUTRAL_LIGHT;
    private boolean loaded;
    private boolean hasActual;
    private boolean commitRequested;
    private boolean skipAll;
    private boolean spaceHeld;
    private long enteredAt = Util.getMillis();
    private long phaseAt = enteredAt;
    private long expiresAt = Long.MAX_VALUE;
    private Phase phase = Phase.ARRIVING;
    private @Nullable RoundView roundView;
    private int hovered = -1;
    private boolean arrivalStarted;
    private boolean activationStarted;
    private long frameAt;
    private int[] reflowPreviousPositions = new int[0];
    private int reflowPreviousCount;
    private float reflowPreviousScroll;
    private boolean reflowStarted;

    public GachaSourceInteractionScreen(@Nullable Screen parent, List<@Nullable ResourceLocation> tables,
                                        List<ItemStackEntry> entries, GachaType animation) {
        super(parent, tables, entries);
        this.animation = animation;
        scene = new GachaSceneRenderer(parent);
    }

    public GachaSourceInteractionScreen(@Nullable Screen parent, GachaType animation, UUID session, long lifetimeMillis) {
        this(parent, List.of(), List.of(), animation);
        this.session = session;
        loaded = true;
        expiresAt = Util.getMillis() + Math.max(0, lifetimeMillis);
    }

    public void setSourceTitle(Component title) {
        sourceTitle = title;
    }

    @Override
    public Component getTitle() {
        return sourceTitle == null ? super.getTitle() : sourceTitle;
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

    public boolean matchesPresentationOptions(GachaPresentationPayload rules) {
        return secondary == rules.secondaryType() && objectCount == rules.objectCount();
    }

    @Override
    public boolean hasReceivedResults() {
        return loaded;
    }

    @Override
    public void setPresentation(GachaPresentationPayload rules) {
        super.setPresentation(rules);
        if (phase != Phase.ARRIVING) {
            return;
        }
        secondary = rules.secondaryType();
        configureObjects(rules.objectCount());
    }

    private void configureObjects(int count) {
        objectCount = secondary == GachaSecondaryType.NONE ? 1
                : Math.clamp(count, 1, GachaSecondaryType.MAXIMUM_ROUNDS);
        completed = new boolean[objectCount];
        selected = secondary == GachaSecondaryType.NONE ? 0 : -1;
        scrollOffset = 0.0F;
        rebuildRemainingObjects();
    }

    private void rebuildRemainingObjects() {
        List<Integer> remaining = new ArrayList<>();

        for (int index = 0; index < objectCount; index++) {
            if (!completed[index]) {
                remaining.add(index);
            }
        }

        remainingObjects = remaining;
        hovered = -1;
    }

    @Override
    public void updateRewards(NonNullList<ItemStack> items) {
        if (loaded) {
            return;
        }
        if (secondary == GachaSecondaryType.MULTIPLE) {
            secondary = GachaSecondaryType.NONE;
            configureObjects(1);
        }
        loadActual(items, List.of());
        loaded = true;
        arrivalStarted = false;
        enteredAt = Util.getMillis();
        phaseAt = enteredAt;
    }

    public void loadCommitted(List<ItemStack> items, List<List<ItemStack>> groups) {
        loadActual(items, groups);
        loaded = true;
        resolveRoundCount();
    }

    private void loadActual(List<ItemStack> items, List<List<ItemStack>> groups) {
        if (hasActual) {
            return;
        }
        hasActual = true;
        aggregate = items.stream().map(ItemStack::copy).toList();
        rounds = groups.stream().map(group -> group.stream().map(ItemStack::copy).toList()).toList();
        for (ItemStack item : aggregate) {
            if (!item.isEmpty()) {
                rewardReceived(item, false);
            }
        }
        lightColor = rewards.stream().max(Comparator.comparingInt(RewardData::getRarityLevel))
                .map(RewardData::getRarityColor).orElse(NEUTRAL_LIGHT);
    }

    private void resolveRoundCount() {
        if (secondary != GachaSecondaryType.MULTIPLE) {
            return;
        }
        if (rounds.isEmpty()) {
            secondary = GachaSecondaryType.NONE;
            objectCount = 1;
            selected = 0;
            completed = new boolean[1];
        } else if (objectCount != rounds.size()) {
            objectCount = rounds.size();
            completed = new boolean[objectCount];
            selected = Math.clamp(selected, 0, objectCount - 1);
        }
        rebuildRemainingObjects();
    }

    public boolean matchesSession(UUID id) {
        return id.equals(session);
    }

    public void receiveResult(boolean success, List<ItemStack> items, List<List<ItemStack>> groups) {
        if (!commitRequested || phase != Phase.WAITING) {
            return;
        }
        if (!success) {
            phase = Phase.FAILED;
            backdrop.close();
            completePresentation();
            return;
        }
        loadActual(items, groups);
        resolveRoundCount();
        if (skipAll) {
            finishAll();
        } else {
            showRound();
        }
    }

    @Override
    protected void init() {
        super.init();
        if (roundView != null) {
            initRoundView();
        }
    }

    private void tickPresentation() {
        if (!loaded || !arrivalStarted || phase == Phase.FINAL || phase == Phase.ROUND || phase == Phase.FAILED || phase == Phase.WAITING) {
            return;
        }
        long now = Util.getMillis();
        if (session != null && !commitRequested && now >= expiresAt) {
            completePresentation();
            PacketDistributor.sendToServer(new FinishGachaItemDrawPayload(session, true));
            phase = Phase.FAILED;
            return;
        }
        if (phase == Phase.ACTIVATING && !activationStarted) {
            return;
        }
        if (phase == Phase.ARRIVING && now - enteredAt >= arrivalMillis()) {
            phase = secondary == GachaSecondaryType.NONE ? Phase.READY : Phase.CHOOSING;
        } else if (phase == Phase.FOCUSING && now - phaseAt >= FOCUS_MILLIS) {
            activateSelected();
        } else if (phase == Phase.ACTIVATING && now - phaseAt >= activationMillis()) {
            phase = Phase.FALLING;
            phaseAt += activationMillis();
        } else if (phase == Phase.FALLING && now - phaseAt >= fallMillis()) {
            revealSelected();
        } else if (phase == Phase.REFLOWING && reflowStarted && now - phaseAt >= REFLOW_MILLIS) {
            phase = Phase.CHOOSING;
            reflowPreviousPositions = new int[0];
        }
    }

    private long arrivalMillis() {
        return animation == GachaTypes.TOTEM ? (long) (GachaSceneRenderer.totemArrivalSeconds() * MILLIS_PER_SECOND)
                : (long) (GachaSceneRenderer.chestArrivalSeconds() * MILLIS_PER_SECOND);
    }

    private long activationMillis() {
        return animation == GachaTypes.CHEST ? GachaChestAnimation.ACTIVATE_MILLIS : GachaTotemImpact.ACTIVATE_MILLIS;
    }

    private long fallMillis() {
        return animation == GachaTypes.CHEST ? GachaChestAnimation.DROP_MILLIS : FALL_MILLIS;
    }

    private void activateSelected() {
        phase = Phase.ACTIVATING;
        phaseAt = Util.getMillis();
        activationStarted = false;
    }

    private void startTotemImpact() {
        if (animation != GachaTypes.TOTEM) {
            return;
        }
        for (Cell cell : cells()) {
            if (cell.index == selected) {
                float focus = selectionFocus(frameAt);
                float x = cell.x + (width / 2.0F - cell.x) * focus;
                float scale = cell.scale + (1.0F - cell.scale) * focus;
                float size = scene.interactionSize(GachaAnimationRenderer.layout(width, height), false) * scale;
                totemImpact.play(frameAt, x, cell.y, size, lightColor);
                return;
            }
        }
    }

    private void revealSelected() {
        if (!hasActual && session != null) {
            if (!commitRequested) {
                commitRequested = true;
                phase = Phase.WAITING;
                PacketDistributor.sendToServer(new FinishGachaItemDrawPayload(session, false));
            }
            return;
        }
        showRound();
    }

    private void showRound() {
        List<ItemStack> items = secondary == GachaSecondaryType.MULTIPLE ? rounds.get(selected) : aggregate;
        roundView = new RoundView(items);
        initRoundView();
        phase = Phase.ROUND;
    }

    private void initRoundView() {
        if (roundView != null) {
            roundView.init(getMinecraft(), Math.max(1, (int) (width * ROUND_WIDTH)),
                    Math.max(1, (int) (height * ROUND_HEIGHT)));
        }
    }

    private void returnFromRound() {
        if (roundView != null) {
            roundView.removed();
            roundView = null;
        }
        List<Integer> previousObjects = remainingObjects;
        float previousScroll = scrollOffset;
        completed[selected] = true;
        rebuildRemainingObjects();
        if (secondary != GachaSecondaryType.MULTIPLE || rounds.size() <= 1) {
            completePresentation();
            onClose();
            return;
        }
        for (boolean done : completed) {
            if (!done) {
                beginReflow(previousObjects, previousScroll);
                return;
            }
        }
        finishAll();
    }

    private void beginReflow(List<Integer> previousObjects, float previousScroll) {
        reflowPreviousPositions = new int[objectCount];

        for (int position = 0; position < previousObjects.size(); position++) {
            reflowPreviousPositions[previousObjects.get(position)] = position;
        }

        reflowPreviousCount = previousObjects.size();
        reflowPreviousScroll = previousScroll;
        reflowStarted = false;
        selected = -1;
        phaseAt = Util.getMillis();
        phase = Phase.REFLOWING;
    }

    private void finishAll() {
        beginSummary();
        phase = Phase.FINAL;
        scene.close();
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    private void renderBackdrop(GuiGraphics graphics) {
        if (!backdrop.renderBackdrop(graphics, lightColor, (Util.getMillis() - enteredAt) / MILLIS_PER_SECOND)) {
            graphics.fill(0, 0, width, height, GachaAnimationRenderer.backgroundColor());
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        tickPresentation();
        renderBackdrop(graphics);
        renderSourceTitle(graphics);
        if (loaded && !arrivalStarted) {
            enteredAt = Util.getMillis();
            phaseAt = enteredAt;
            arrivalStarted = true;
        }
        if (phase == Phase.REFLOWING && !reflowStarted) {
            phaseAt = Util.getMillis();
            reflowStarted = true;
        }
        frameAt = Util.getMillis();
        if (phase == Phase.ACTIVATING && !activationStarted) {
            phaseAt = frameAt;
            activationStarted = true;
            startTotemImpact();
        }
        if (roundView != null) {
            int left = (width - roundView.width) / 2;
            int top = (height - roundView.height) / 2;
            graphics.pose().pushPose();
            try {
                graphics.pose().translate(left, top, 0.0F);
                roundView.render(graphics, mouseX - left, mouseY - top, partialTick);
            } finally {
                graphics.pose().popPose();
            }
            return;
        }
        if (phase == Phase.FINAL) {
            renderSummaryItems(graphics, mouseX, mouseY, partialTick);
            return;
        }
        hovered = -1;
        if (phase == Phase.CHOOSING && withinRow(mouseX)) {
            for (Cell cell : cells()) {
                if (!completed[cell.index] && cell.contains(mouseX, mouseY)) {
                    hovered = cell.index;
                    break;
                }
            }
        } else if (phase == Phase.READY && !cells().isEmpty() && cells().getFirst().contains(mouseX, mouseY)) {
            hovered = selected;
        }
        if (loaded && phase != Phase.WAITING && phase != Phase.FAILED) {
            drawObjects(graphics);
            totemImpact.render(graphics, frameAt);
        }
        String hint = !loaded || phase == Phase.WAITING ? TranslationKeys.GUI_ANIMATION_WAITING
                : phase == Phase.FAILED ? TranslationKeys.GUI_ANIMATION_FAILED
                : phase == Phase.CHOOSING ? TranslationKeys.GUI_SECONDARY_CHOOSE
                : phase == Phase.READY ? TranslationKeys.GUI_SECONDARY_TRIGGER : TranslationKeys.GUI_SECONDARY_ANIMATING;
        graphics.drawCenteredString(font, Component.translatable(hint), width / 2, height - FOOTER_MARGIN, TEXT_COLOR);
        Row row = rowLayout();
        if (row.maximumScroll > 0.0F && phase == Phase.CHOOSING) {
            int left = (int) ((width - row.width) / 2.0F);
            int top = (int) (height / 2.0F + row.pitch / 2.0F);
            int thumb = Math.max(1, (int) (row.width * row.width / (remainingObjects.size() * row.pitch)));
            int offset = (int) ((row.width - thumb) * scrollOffset / row.maximumScroll);
            graphics.fill(left, top, left + (int) row.width, top + SCROLL_TRACK_HEIGHT, SCROLL_TRACK_COLOR);
            graphics.fill(left + offset, top, left + offset + thumb, top + SCROLL_TRACK_HEIGHT, SCROLL_THUMB_COLOR);
        }
    }

    private Row rowLayout() {
        float size = scene.interactionSize(GachaAnimationRenderer.layout(width, height), animation == GachaTypes.CHEST);
        float shownSize = objectCount == 1 ? size : Math.min(size * MULTIPLE_SCALE, height * ROW_HEIGHT);
        float pitch = Math.max(1.0F, shownSize / CELL_CONTENT);
        float visibleWidth = width * GRID_WIDTH;
        float maximumScroll = Math.max(0.0F, remainingObjects.size() * pitch - visibleWidth);
        scrollOffset = Math.clamp(scrollOffset, 0.0F, maximumScroll);
        return new Row(pitch, shownSize / size, visibleWidth, maximumScroll);
    }

    private boolean withinRow(double mouseX) {
        return mouseX >= width * (1.0F - GRID_WIDTH) / 2.0F
                && mouseX < width * (1.0F + GRID_WIDTH) / 2.0F;
    }

    private List<Cell> cells() {
        Row row = rowLayout();
        float contentWidth = remainingObjects.size() * row.pitch;
        float left = contentWidth <= row.width ? (width - contentWidth) / 2.0F
                : (width - row.width) / 2.0F - scrollOffset;
        int first = Math.max(0, (int) (scrollOffset / row.pitch) - 1);
        int last = Math.min(remainingObjects.size(), first + (int) Math.ceil(row.width / row.pitch) + 2);
        float previousLeft = left;
        float progress = 1.0F;
        if (phase == Phase.REFLOWING) {
            float previousWidth = reflowPreviousCount * row.pitch;
            float previousScroll = Math.clamp(reflowPreviousScroll, 0.0F, Math.max(0.0F, previousWidth - row.width));
            previousLeft = previousWidth <= row.width ? (width - previousWidth) / 2.0F
                    : (width - row.width) / 2.0F - previousScroll;
            progress = Math.clamp((float) (frameAt - phaseAt) / REFLOW_MILLIS, 0.0F, 1.0F);
            progress = progress * progress * (3.0F - 2.0F * progress);
        }

        List<Cell> result = new ArrayList<>();
        for (int index = first; index < last; index++) {
            int object = remainingObjects.get(index);
            float x = left + (index + 0.5F) * row.pitch;
            if (phase == Phase.REFLOWING) {
                float previousX = previousLeft + (reflowPreviousPositions[object] + 0.5F) * row.pitch;
                x = previousX + (x - previousX) * progress;
            }
            result.add(new Cell(object, x, height / 2.0F, row.scale, row.pitch * CELL_CONTENT / 2.0F));
        }
        return result;
    }

    private void drawObjects(GuiGraphics graphics) {
        int left = (int) (width * (1.0F - GRID_WIDTH) / 2.0F);
        int right = (int) (width * (1.0F + GRID_WIDTH) / 2.0F);
        graphics.enableScissor(left, 0, right, height);
        try {
            drawVisibleObjects(graphics);
        } finally {
            graphics.disableScissor();
        }
    }

    private void drawVisibleObjects(GuiGraphics graphics) {
        scene.beginInteractionFrame();
        List<Cell> cells = cells();
        if (phase != Phase.ARRIVING && phase != Phase.FALLING) {
            boolean chest = animation == GachaTypes.CHEST;
            float size = scene.interactionSize(GachaAnimationRenderer.layout(width, height), chest);
            float opacity = GachaAnimationRenderer.smoothProgress((float) (frameAt - enteredAt - arrivalMillis()) / REFLECTION_FADE_MILLIS);
            float focus = selectionFocus(frameAt);
            for (Cell cell : cells) {
                if (!completed[cell.index]) {
                    float scale = cell.index == selected ? cell.scale + (1.0F - cell.scale) * focus
                            : cell.scale * (1.0F - focus * OTHER_SHRINK);
                    if (cell.index == selected && animation == GachaTypes.TOTEM && phase == Phase.ACTIVATING) {
                        scale *= GachaTotemImpact.swell(frameAt - phaseAt);
                    }
                    scene.renderReflection(graphics, cell.y + size * scale * (chest ? CHEST_REFLECTION_BASE : ITEM_REFLECTION_BASE), opacity,
                            () -> drawObject(graphics, cell, cell.index == selected));
                }
            }
        }
        for (Cell cell : cells) {
            if (!completed[cell.index] && cell.index != selected) {
                drawObject(graphics, cell, false);
            }
        }
        for (Cell cell : cells) {
            if (!completed[cell.index] && cell.index == selected) {
                drawObject(graphics, cell, true);
            }
        }
        for (Cell cell : cells) {
            if (!completed[cell.index] && cell.index == hovered) {
                scene.renderHovered(graphics, () -> drawObject(graphics, cell, cell.index == selected));
                break;
            }
        }
    }

    private float selectionFocus(long now) {
        float focus = secondary == GachaSecondaryType.SELECT_ONE && selected >= 0
                ? phase == Phase.FOCUSING ? Math.clamp((float) (now - phaseAt) / FOCUS_MILLIS, 0.0F, 1.0F) : 1.0F : 0.0F;
        return focus * focus * (3.0F - 2.0F * focus);
    }

    private void drawObject(GuiGraphics graphics, Cell cell, boolean active) {
        long now = frameAt;
        float focus = selectionFocus(now);
        float x = active ? cell.x + (width / 2.0F - cell.x) * focus : cell.x;
        float y = active ? cell.y + (height / 2.0F - cell.y) * focus : cell.y;
        float scale = active ? cell.scale + (1.0F - cell.scale) * focus : cell.scale * (1.0F - focus * OTHER_SHRINK);
        if (active && animation == GachaTypes.TOTEM && phase == Phase.ACTIVATING) {
            scale *= GachaTotemImpact.swell(now - phaseAt);
        }
        float opening = active && animation == GachaTypes.CHEST && (phase == Phase.ACTIVATING || phase == Phase.FALLING)
                ? phase == Phase.FALLING ? 1.0F : GachaChestAnimation.opening(now - phaseAt) : 0.0F;
        if (!active && selected >= 0 && (phase == Phase.FOCUSING || phase == Phase.ACTIVATING || phase == Phase.FALLING)) {
            float dismiss = phase == Phase.FOCUSING ? Math.clamp((float) (now - phaseAt) / FOCUS_MILLIS, 0.0F, 1.0F)
                    : secondary == GachaSecondaryType.MULTIPLE && phase == Phase.ACTIVATING
                    ? Math.clamp((float) (now - phaseAt) / activationMillis(), 0.0F, 1.0F) : 1.0F;
            y += height * FALL_DISTANCE * dismiss * dismiss;
        }
        if (active && phase == Phase.FALLING) {
            float fall = Math.clamp((float) (now - phaseAt) / fallMillis(), 0.0F, 1.0F);
            y += height * FALL_DISTANCE * (animation == GachaTypes.CHEST ? GachaAnimationRenderer.smoothProgress(fall) : fall * fall);
        }
        float seconds = (now - enteredAt) / MILLIS_PER_SECOND;
        float arrival = Math.clamp((float) (now - enteredAt) / arrivalMillis(), 0.0F, 1.0F);
        float straighten = Math.clamp((arrival - STRAIGHTEN_START) / (1.0F - STRAIGHTEN_START), 0.0F, 1.0F);
        boolean chest = animation == GachaTypes.CHEST;
        scene.renderInteraction(graphics, GachaAnimationRenderer.layout(width, height), getTriggerItem(), chest,
                phase == Phase.ARRIVING, seconds, straighten,
                x, y, scale, opening, lightColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (roundView != null) {
            return roundView.mouseClicked(mouseX - (width - roundView.width) / 2,
                    mouseY - (height - roundView.height) / 2, button);
        }
        if (phase == Phase.FINAL) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !loaded) {
            return false;
        }
        if (phase == Phase.CHOOSING && withinRow(mouseX)) {
            for (Cell cell : cells()) {
                if (!completed[cell.index] && cell.contains(mouseX, mouseY)) {
                    selected = cell.index;
                    if (secondary == GachaSecondaryType.SELECT_ONE) {
                        phase = Phase.FOCUSING;
                        phaseAt = Util.getMillis();
                    } else {
                        activateSelected();
                    }
                    return true;
                }
            }
        } else if (phase == Phase.READY) {
            activateSelected();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (roundView != null) {
            return roundView.keyPressed(keyCode, scanCode, modifiers);
        }
        if (phase == Phase.FINAL) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            if (!spaceHeld && loaded && !isForceWatch() && phase != Phase.FAILED) {
                spaceHeld = true;
                skipAll = true;
                if (hasActual) {
                    finishAll();
                } else {
                    revealSelected();
                }
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            spaceHeld = false;
        }
        return roundView != null ? roundView.keyReleased(keyCode, scanCode, modifiers)
                : super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (roundView != null) {
            return roundView.mouseScrolled(mouseX - (width - roundView.width) / 2,
                    mouseY - (height - roundView.height) / 2, scrollX, scrollY);
        }
        if (phase == Phase.FINAL) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        double direction = scrollX != 0.0 ? scrollX : scrollY;
        if (phase == Phase.CHOOSING && direction != 0.0) {
            Row row = rowLayout();
            scrollOffset = Math.clamp(scrollOffset - (float) direction * row.pitch * SCROLL_STEP, 0.0F, row.maximumScroll);
            return true;
        }
        return false;
    }

    @Override
    public void removed() {
        scene.close();
        backdrop.close();
        if (roundView != null) {
            roundView.removed();
        }
        if (session != null && !hasActual && minecraft != null && minecraft.getConnection() != null) {
            PacketDistributor.sendToServer(new FinishGachaItemDrawPayload(session, true));
        }
        super.removed();
    }

    private enum Phase {
        ARRIVING, CHOOSING, FOCUSING, READY, ACTIVATING, FALLING, WAITING, ROUND, REFLOWING, FINAL, FAILED
    }

    private record Row(float pitch, float scale, float width, float maximumScroll) {
    }

    private record Cell(int index, float x, float y, float scale, float half) {
        private boolean contains(double mouseX, double mouseY) {
            return Math.abs(mouseX - x) <= half && Math.abs(mouseY - y) <= half;
        }
    }

    private class RoundView extends GachaSummaryScreen {
        private boolean ready;

        private RoundView(List<ItemStack> items) {
            super(null, List.of(), List.of());
            NonNullList<ItemStack> result = NonNullList.create();
            items.forEach(item -> result.add(item.copy()));
            updateRewards(result, true);
        }

        @Override
        public Component getTitle() {
            return GachaSourceInteractionScreen.this.getTitle();
        }

        @Override
        public boolean isForceWatch() {
            return GachaSourceInteractionScreen.this.isForceWatch();
        }

        @Override
        protected void completePresentation() {
            ready = true;
        }

        @Override
        public void onClose() {
            if (ready || !isForceWatch()) {
                returnFromRound();
            }
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderSummaryItems(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        protected void renderSummaryTooltip(GuiGraphics graphics, ItemStack item, int mouseX, int mouseY) {
            int left = (GachaSourceInteractionScreen.this.width - width) / 2;
            int top = (GachaSourceInteractionScreen.this.height - height) / 2;
            graphics.pose().pushPose();
            try {
                graphics.pose().translate(-left, -top, 0.0F);
                super.renderSummaryTooltip(graphics, item, mouseX + left, mouseY + top);
            } finally {
                graphics.pose().popPose();
            }
        }
    }
}
