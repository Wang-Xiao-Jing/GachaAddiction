package xiaojin.gachaaddiction.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.registry.item.GachaDrawItems;


public class GachaSceneRenderer implements AutoCloseable {
    private static final long SOURCE_LIFETIME_MILLIS = 5000;
    private static final float NO_OVERRIDE = -1.0F;
    private static final float CHEST_PITCH = 15.0F;
    private static final float CHEST_YAW = 45.0F;
    private static final float SCENE_SCALE = 0.42F;
    private static final float DOUBLE_CHEST_SCALE = 0.75F;
    private static final float SCENE_BASE_Y = 0.66F;
    private static final float SCENE_DEPTH = 300.0F;
    private static final float CHEST_FIELD_OF_VIEW = 35.0F;
    private static final float CHEST_CAMERA_DISTANCE = 4.0F;
    private static final float CHEST_NEAR_PLANE = 0.05F;
    private static final float CHEST_FAR_PLANE = 100.0F;
    private static final float CENTER = 0.5F;
    private static final float FULL_TURN = (float) (Math.PI * 2.0);
    private static final float LID_HEIGHT = 0.58F;
    private static final float LIGHT_HEIGHT = 1.65F;
    private static final float LIGHT_RADIUS = 0.8F;
    private static final float LIGHT_BASE_RADIUS = 0.2F;
    private static final int LIGHT_SEGMENTS = 4;
    private static final int LIGHT_BANDS = 4;
    private static final int LIGHT_ALPHA = 52;
    private static final int PARTICLES = 12;
    private static final float PARTICLE_SIZE = 0.025F;
    private static final float PARTICLE_RATE = 0.7F;
    private static final float PARTICLE_ROTATION = 0.35F;
    private static final float TOTEM_DURATION_SECONDS = 2.0F;
    // 原版缩放 sin(PI * curve) 第一次达到1的进度，此后物品开始缩回。
    private static final float TOTEM_CLOSEST_PROGRESS = 0.53173644F;
    private static final float TOTEM_PEAK_SIZE = 225.0F;
    private static final float TOTEM_UPRIGHT_ROTATION = 900.0F;
    private static final float TOTEM_FRONT_ROTATION = 180.0F;
    private static final float INTERACTION_BOX_SIZE = 1.5F;
    private static final float INTERACTION_ENTRY_SECONDS = 0.65F;
    private static final float INTERACTION_ENTRY_YAW = -70.0F;
    private static final float INTERACTION_ENTRY_HEIGHT = 0.3F;
    private static final float REFLECTION_COMPRESSION = 0.35F;
    private static final float REFLECTION_OPACITY = 0.22F;
    private static final float REFLECTION_GAP = 3.0F;
    private static final Vector3f FRONT_LIGHT = new Vector3f(0.25F, 0.5F, 1.0F).normalize();
    private static final Vector3f FRONT_FILL_LIGHT = new Vector3f(-0.25F, 0.2F, 1.0F).normalize();
    private static final String CHEST_RENDER_WARNING = "Cannot render source chest in gacha scene; using a vanilla chest";
    private static final RenderType LIGHT = RenderType.create(GachaAddiction.MODID + ":gacha_scene_light",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1024, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderType.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderType.NO_CULL)
                    .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .createCompositeState(false));
    private static final ThreadLocal<ActiveChest> ACTIVE_CHEST = new ThreadLocal<>();
    private static @Nullable PendingSource pending;
    private @Nullable RenderTarget chestTarget;
    private @Nullable RenderTarget reflectionTarget;
    private final GachaObjectOutline outline = new GachaObjectOutline();
    private @Nullable RenderTarget outputOverride;
    private float interactionYaw = CHEST_YAW;
    private float cachedYaw;
    private int frameRevision;
    private int cachedFrame = -1;
    private float cachedOpening;
    private int cachedColor;
    private ItemStack cachedTrigger = ItemStack.EMPTY;
    private boolean frameScoped;
    private int lightColor;
    private final float activationOffsetX = RandomSource.create().nextFloat() * 2.0F - 1.0F;
    private final float activationOffsetY = RandomSource.create().nextFloat() * 2.0F - 1.0F;
    private @Nullable ChestBlockEntity source;
    private ItemStack triggerItem = ItemStack.EMPTY;
    private final ChestBlockEntity fallback = new ChestBlockEntity(BlockPos.ZERO,
            Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));

    public GachaSceneRenderer(@Nullable Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        PendingSource candidate = pending;
        pending = null;
        if (candidate == null || minecraft.level != candidate.level
                || Util.getMillis() - candidate.createdAt > SOURCE_LIFETIME_MILLIS) {
            return;
        }

        if (candidate.requiresNewMenu && (!(parent instanceof MenuAccess<?> menu)
                || minecraft.player == null || menu.getMenu() != minecraft.player.containerMenu
                || menu.getMenu().containerId == candidate.previousMenuId)) {
            return;
        }

        triggerItem = candidate.display;
        if (candidate.chest != null && !candidate.chest.isRemoved()) {
            source = candidate.chest;
        }
    }

    public static void rememberSource(BlockPos position) {
        pending = null;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            ChestBlockEntity chest = minecraft.level.getBlockEntity(position) instanceof ChestBlockEntity holder
                    ? holder : null;
            pending = new PendingSource(chest, new ItemStack(minecraft.level.getBlockState(position).getBlock()),
                    minecraft.level, minecraft.player.containerMenu.containerId, true, Util.getMillis());
        }
    }

    public static void rememberItemSource(ItemStack item) {
        pending = null;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null && !item.isEmpty()) {
            pending = new PendingSource(null, item.copyWithCount(1), minecraft.level,
                    minecraft.player.containerMenu.containerId, false, Util.getMillis());
        }
    }

    public static void rememberEntitySource(Entity entity) {
        pending = null;
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack display = entity.getPickResult();
        if (minecraft.level != null && minecraft.player != null && display != null && !display.isEmpty()) {
            pending = new PendingSource(null, display.copyWithCount(1), minecraft.level,
                    minecraft.player.containerMenu.containerId, true, Util.getMillis());
        }
    }

    public static void clearSource() {
        pending = null;
    }

    public static float overrideOpenNess(ChestBlockEntity chest) {
        ActiveChest active = ACTIVE_CHEST.get();
        return active != null && (active.first == chest || active.second == chest) ? active.opening : NO_OVERRIDE;
    }

    public static float overrideRenderedOpenNess() {
        ActiveChest active = ACTIVE_CHEST.get();
        return active == null ? NO_OVERRIDE : active.opening;
    }

    public static boolean isChestItem(ItemStack trigger) {
        return trigger.is(GachaDrawItems.SEALED_CHEST)
                || trigger.getItem() instanceof BlockItem item && item.getBlock() instanceof ChestBlock;
    }

    public static float totemArrivalSeconds() {
        return TOTEM_DURATION_SECONDS * TOTEM_CLOSEST_PROGRESS;
    }

    public static float chestArrivalSeconds() {
        return INTERACTION_ENTRY_SECONDS;
    }

    public static boolean hasTotemReachedClosest(float seconds) {
        return seconds / TOTEM_DURATION_SECONDS >= TOTEM_CLOSEST_PROGRESS;
    }

    private static void renderItem(GuiGraphics graphics, ItemStack item) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemRenderer().renderStatic(item, ItemDisplayContext.NONE, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, graphics.pose(), graphics.bufferSource(), minecraft.level, 0);
    }

    public void rememberForReplacement(ItemStack display) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            pending = new PendingSource(source, display.copyWithCount(1), minecraft.level,
                    minecraft.player.containerMenu.containerId, false, Util.getMillis());
        }
    }

    public ItemStack getTriggerItem() {
        return triggerItem;
    }

    public void renderChest(GuiGraphics graphics, GachaAnimationRenderer.Layout layout,
                            float seconds, float opening, ItemStack trigger, int color) {
        if (frameScoped && cachedFrame == frameRevision && cachedOpening == opening && cachedColor == color && cachedYaw == interactionYaw
                && ItemStack.isSameItemSameComponents(cachedTrigger, trigger) && chestTarget != null
                && chestTarget.width == Minecraft.getInstance().getMainRenderTarget().width
                && chestTarget.height == Minecraft.getInstance().getMainRenderTarget().height) {
            compositeChest(graphics);
            return;
        }
        lightColor = color;
        Minecraft minecraft = Minecraft.getInstance();
        ChestBlockEntity chest = source;
        if (chest == null || chest.isRemoved() || chest.getLevel() != minecraft.level
                || !(chest.getBlockState().getBlock() instanceof ChestBlock)) {
            chest = fallback;
        }
        ChestBlockEntity neighbor = neighbor(chest);
        RenderTarget mainTarget = outputOverride == null ? minecraft.getMainRenderTarget() : outputOverride;
        GachaRenderState previousState = new GachaRenderState();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            graphics.flush();
            RenderSystem.disableScissor();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);

            if (chestTarget == null || chestTarget.width != mainTarget.width || chestTarget.height != mainTarget.height) {
                if (chestTarget != null) {
                    chestTarget.destroyBuffers();
                }
                chestTarget = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
                chestTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            }
            chestTarget.clear(Minecraft.ON_OSX);
            chestTarget.bindWrite(true);
            float aspect = chestTarget.width / (float) chestTarget.height;
            float fieldOfView = (float) Math.toRadians(CHEST_FIELD_OF_VIEW);
            RenderSystem.setProjectionMatrix(new Matrix4f().perspective(fieldOfView, aspect,
                    CHEST_NEAR_PLANE, CHEST_FAR_PLANE), VertexSorting.DISTANCE_TO_ORIGIN);
            RenderSystem.getModelViewStack().identity();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            pose.last().pose().identity();
            pose.last().normal().identity();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderLights(FRONT_LIGHT, FRONT_FILL_LIGHT);
            float halfViewHeight = CHEST_CAMERA_DISTANCE * (float) Math.tan(fieldOfView / 2.0F);
            float scale = Math.min(layout.width(), layout.height()) * SCENE_SCALE
                    / graphics.guiHeight() * halfViewHeight * 2.0F
                    * (neighbor == null ? 1.0F : DOUBLE_CHEST_SCALE);
            pose.translate((layout.x(CENTER) / (float) graphics.guiWidth() * 2.0F - 1.0F) * halfViewHeight * aspect,
                    (1.0F - layout.y(SCENE_BASE_Y) / (float) graphics.guiHeight() * 2.0F) * halfViewHeight,
                    -CHEST_CAMERA_DISTANCE);
            pose.scale(scale, scale, scale);
            pose.mulPose(Axis.XP.rotationDegrees(CHEST_PITCH));
            pose.mulPose(Axis.YP.rotationDegrees(interactionYaw));
            try {
                if (!trigger.isEmpty() && trigger.has(DataComponents.CUSTOM_MODEL_DATA)) {
                    renderItem(graphics, trigger);
                } else {
                    renderChestModel(graphics, chest, neighbor, opening);
                }
            } catch (RuntimeException | LinkageError failure) {
                if (chest == fallback) {
                    throw failure;
                }
                GachaAddiction.LOGGER.warn(CHEST_RENDER_WARNING, failure);
                source = null;
                renderChestModel(graphics, fallback, null, opening);
            }
            graphics.flush();
            if (opening > 0.0F) {
                renderLight(graphics, seconds, opening);
                graphics.flush();
            }
        } finally {
            pose.popPose();
            Lighting.setupFor3DItems();
            try {
                mainTarget.bindWrite(true);
            } finally {
                previousState.close();
            }
        }
        cachedFrame = frameRevision;
        cachedOpening = opening;
        cachedColor = color;
        cachedYaw = interactionYaw;
        cachedTrigger = trigger;
        compositeChest(graphics);
    }

    public void beginInteractionFrame() {
        frameScoped = true;
        frameRevision++;
    }

    public float interactionSize(GachaAnimationRenderer.Layout layout, boolean chest) {
        return chest ? Math.min(layout.width(), layout.height()) * SCENE_SCALE * INTERACTION_BOX_SIZE : TOTEM_PEAK_SIZE;
    }

    public void renderHovered(GuiGraphics graphics, Runnable draw) {
        outline.render(graphics, target -> {
            outputOverride = target;
            try {
                draw.run();
            } finally {
                outputOverride = null;
            }
        });
    }

    public void renderInteraction(GuiGraphics graphics, GachaAnimationRenderer.Layout layout, ItemStack trigger,
                                  boolean chest, boolean arriving, float seconds, float straighten,
                                  float centerX, float centerY, float scale, float opening, int color) {
        float anchorX = graphics.guiWidth() * CENTER;
        float anchorY = chest ? layout.y(SCENE_BASE_Y)
                - Math.min(layout.width(), layout.height()) * SCENE_SCALE * CENTER : graphics.guiHeight() * CENTER;
        float entry = chest && arriving ? Math.clamp(seconds / INTERACTION_ENTRY_SECONDS, 0.0F, 1.0F) : 1.0F;
        entry = GachaAnimationRenderer.smoothProgress(entry);
        float previousYaw = interactionYaw;
        interactionYaw = INTERACTION_ENTRY_YAW * (1.0F - entry);
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(centerX, centerY + (1.0F - entry) * graphics.guiHeight() * INTERACTION_ENTRY_HEIGHT, 0.0F);
            float entryScale = scale * (CENTER + CENTER * entry);
            graphics.pose().scale(entryScale, entryScale, 1.0F);
            graphics.pose().translate(-anchorX, -anchorY, 0.0F);
            if (chest) {
                renderChest(graphics, layout, seconds, opening, trigger, color);
            } else if (arriving) {
                float progress = GachaAnimationRenderer.smoothProgress(seconds / totemArrivalSeconds());
                renderTotem(graphics, progress * totemArrivalSeconds(), trigger,
                        GachaAnimationRenderer.smoothProgress(straighten));
            } else {
                renderReadyItem(graphics, trigger);
            }
        } finally {
            graphics.pose().popPose();
            interactionYaw = previousYaw;
        }
    }

    private void renderReadyItem(GuiGraphics graphics, ItemStack trigger) {
        if (trigger.isEmpty()) {
            return;
        }

        try (GachaRenderState previousState = new GachaRenderState()) {
            graphics.pose().pushPose();
            try {
                graphics.flush();
                RenderSystem.setShaderLights(FRONT_LIGHT, FRONT_FILL_LIGHT);
                graphics.pose().translate(graphics.guiWidth() * CENTER, graphics.guiHeight() * CENTER, SCENE_DEPTH);
                graphics.pose().scale(TOTEM_PEAK_SIZE, -TOTEM_PEAK_SIZE, TOTEM_PEAK_SIZE);
                graphics.pose().mulPose(Axis.YP.rotationDegrees(TOTEM_FRONT_ROTATION));
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.getItemRenderer().renderStatic(trigger, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY, graphics.pose(), graphics.bufferSource(), minecraft.level, 0);
                graphics.flush();
            } finally {
                graphics.pose().popPose();
                Lighting.setupFor3DItems();
            }
        }
    }

    public void renderReflection(GuiGraphics graphics, float baseline, float opacity, Runnable draw) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        if (opacity <= 0.0F || baseline <= 0.0F || baseline >= graphics.guiHeight()) {
            return;
        }

        try (GachaRenderState previousState = new GachaRenderState()) {
            graphics.flush();
            RenderSystem.disableScissor();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            if (reflectionTarget == null || reflectionTarget.width != mainTarget.width || reflectionTarget.height != mainTarget.height) {
                if (reflectionTarget != null) {
                    reflectionTarget.destroyBuffers();
                }
                reflectionTarget = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
                reflectionTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            }
            reflectionTarget.clear(Minecraft.ON_OSX);
            reflectionTarget.bindWrite(true);
            outputOverride = reflectionTarget;
            try {
                draw.run();
                graphics.flush();
            } finally {
                outputOverride = null;
                mainTarget.bindWrite(true);
            }
        }

        try (GachaRenderState previousState = new GachaRenderState()) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, reflectionTarget.getColorTextureId());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            float top = baseline + REFLECTION_GAP;
            float bottom = top + baseline * REFLECTION_COMPRESSION;
            float uv = 1.0F - baseline / graphics.guiHeight();
            float alpha = REFLECTION_OPACITY * opacity;
            Matrix4f matrix = graphics.pose().last().pose();
            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.addVertex(matrix, 0.0F, top, SCENE_DEPTH).setUv(0.0F, uv).setColor(alpha, alpha, alpha, alpha);
            buffer.addVertex(matrix, 0.0F, bottom, SCENE_DEPTH).setUv(0.0F, 1.0F).setColor(0, 0, 0, 0);
            buffer.addVertex(matrix, graphics.guiWidth(), bottom, SCENE_DEPTH).setUv(1.0F, 1.0F).setColor(0, 0, 0, 0);
            buffer.addVertex(matrix, graphics.guiWidth(), top, SCENE_DEPTH).setUv(1.0F, uv).setColor(alpha, alpha, alpha, alpha);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }
    }

    private void compositeChest(GuiGraphics graphics) {
        if (chestTarget == null) {
            return;
        }

        try (GachaRenderState previousState = new GachaRenderState()) {
            graphics.flush();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, chestTarget.getColorTextureId());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            Matrix4f matrix = graphics.pose().last().pose();
            buffer.addVertex(matrix, 0.0F, 0.0F, SCENE_DEPTH).setUv(0.0F, 1.0F);
            buffer.addVertex(matrix, 0.0F, graphics.guiHeight(), SCENE_DEPTH).setUv(0.0F, 0.0F);
            buffer.addVertex(matrix, graphics.guiWidth(), graphics.guiHeight(), SCENE_DEPTH).setUv(1.0F, 0.0F);
            buffer.addVertex(matrix, graphics.guiWidth(), 0.0F, SCENE_DEPTH).setUv(1.0F, 1.0F);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }
    }

    @Override
    public void close() {
        outline.close();
        outputOverride = null;
        if (chestTarget == null && reflectionTarget == null) {
            return;
        }

        try (GachaRenderState previousState = new GachaRenderState()) {
            try {
                if (reflectionTarget != null) {
                    reflectionTarget.destroyBuffers();
                    reflectionTarget = null;
                }
                if (chestTarget != null) {
                    chestTarget.destroyBuffers();
                    chestTarget = null;
                }
            } finally {
                Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            }
        }
    }

    private @Nullable ChestBlockEntity neighbor(ChestBlockEntity chest) {
        var state = chest.getBlockState();
        var level = chest.getLevel();
        if (level != null && state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos position = chest.getBlockPos().relative(ChestBlock.getConnectedDirection(state));
            if (level.getBlockState(position).getBlock() == state.getBlock()
                    && level.getBlockEntity(position) instanceof ChestBlockEntity other) {
                return other;
            }
        }
        return null;
    }

    private void renderChestModel(GuiGraphics graphics, ChestBlockEntity chest,
                                  @Nullable ChestBlockEntity neighbor, float opening) {
        ActiveChest previous = ACTIVE_CHEST.get();
        ACTIVE_CHEST.set(new ActiveChest(chest, neighbor, opening));
        PoseStack pose = new PoseStack();
        pose.last().pose().set(graphics.pose().last().pose());
        pose.last().normal().set(graphics.pose().last().normal());
        float dx = neighbor == null ? 0 : neighbor.getBlockPos().getX() - chest.getBlockPos().getX();
        float dz = neighbor == null ? 0 : neighbor.getBlockPos().getZ() - chest.getBlockPos().getZ();
        try {
            pose.mulPose(Axis.YP.rotationDegrees(chest.getBlockState().getValue(ChestBlock.FACING).toYRot()));
            pose.translate(-CENTER - dx * CENTER, 0.0F, -CENTER - dz * CENTER);
            renderBlockEntity(graphics, pose, chest);
            if (neighbor != null) {
                pose.translate(dx, 0.0F, dz);
                renderBlockEntity(graphics, pose, neighbor);
            }
        } finally {
            if (previous == null) {
                ACTIVE_CHEST.remove();
            } else {
                ACTIVE_CHEST.set(previous);
            }
        }
    }

    private void renderBlockEntity(GuiGraphics graphics, PoseStack pose, ChestBlockEntity chest) {
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(chest);
        if (renderer == null) {
            throw new IllegalStateException("Chest has no block entity renderer");
        }
        renderer.render(chest, 0.0F, pose, graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    private void renderLight(GuiGraphics graphics, float seconds, float opening) {
        VertexConsumer vertices = graphics.bufferSource().getBuffer(LIGHT);
        Matrix4f matrix = graphics.pose().last().pose();
        for (int band = 0; band < LIGHT_BANDS; band++) {
            float lower = band / (float) LIGHT_BANDS;
            float upper = (band + 1) / (float) LIGHT_BANDS;
            float lowerRadius = LIGHT_BASE_RADIUS + lower * LIGHT_RADIUS * opening;
            float upperRadius = LIGHT_BASE_RADIUS + upper * LIGHT_RADIUS * opening;
            float lowerY = LID_HEIGHT + lower * LIGHT_HEIGHT * opening;
            float upperY = LID_HEIGHT + upper * LIGHT_HEIGHT * opening;
            int lowerAlpha = (int) (LIGHT_ALPHA * opening * (1.0F - lower));
            int upperAlpha = (int) (LIGHT_ALPHA * opening * (1.0F - upper));
            for (int segment = 0; segment < LIGHT_SEGMENTS; segment++) {
                int next = (segment + 1) % LIGHT_SEGMENTS;
                float x = segment == 0 || segment == 3 ? -1.0F : 1.0F;
                float z = segment < 2 ? -1.0F : 1.0F;
                float nextX = next == 0 || next == 3 ? -1.0F : 1.0F;
                float nextZ = next < 2 ? -1.0F : 1.0F;
                vertex(vertices, matrix, x * lowerRadius, lowerY, z * lowerRadius, lowerAlpha);
                vertex(vertices, matrix, nextX * lowerRadius, lowerY, nextZ * lowerRadius, lowerAlpha);
                vertex(vertices, matrix, nextX * upperRadius, upperY, nextZ * upperRadius, upperAlpha);
                vertex(vertices, matrix, x * upperRadius, upperY, z * upperRadius, upperAlpha);
            }
        }
        for (int index = 0; index < PARTICLES; index++) {
            float phase = (seconds * PARTICLE_RATE + index / (float) PARTICLES) % 1.0F;
            float angle = index * FULL_TURN / PARTICLES + seconds * PARTICLE_ROTATION;
            float radius = LIGHT_BASE_RADIUS + phase * LIGHT_RADIUS * opening;
            float x = (float) Math.cos(angle);
            float z = (float) Math.sin(angle);
            float squareScale = radius / Math.max(Math.abs(x), Math.abs(z));
            x *= squareScale;
            z *= squareScale;
            float y = LID_HEIGHT + phase * LIGHT_HEIGHT * opening;
            float half = PARTICLE_SIZE * opening;
            int alpha = (int) (255 * opening * (1.0F - phase));
            particle(vertices, matrix, x, y, z, half, alpha);
        }
    }

    private void particle(VertexConsumer vertices, Matrix4f matrix, float x, float y, float z, float half, int alpha) {
        face(vertices, matrix, x - half, y - half, z - half, x + half, y + half, z - half, false, alpha);
        face(vertices, matrix, x - half, y - half, z + half, x + half, y + half, z + half, false, alpha);
        face(vertices, matrix, x - half, y - half, z - half, x - half, y + half, z + half, true, alpha);
        face(vertices, matrix, x + half, y - half, z - half, x + half, y + half, z + half, true, alpha);
        vertex(vertices, matrix, x - half, y + half, z - half, alpha);
        vertex(vertices, matrix, x + half, y + half, z - half, alpha);
        vertex(vertices, matrix, x + half, y + half, z + half, alpha);
        vertex(vertices, matrix, x - half, y + half, z + half, alpha);
        vertex(vertices, matrix, x - half, y - half, z - half, alpha);
        vertex(vertices, matrix, x + half, y - half, z - half, alpha);
        vertex(vertices, matrix, x + half, y - half, z + half, alpha);
        vertex(vertices, matrix, x - half, y - half, z + half, alpha);
    }

    private void face(VertexConsumer vertices, Matrix4f matrix, float x0, float y0, float z0,
                      float x1, float y1, float z1, boolean side, int alpha) {
        vertex(vertices, matrix, x0, y0, z0, alpha);
        vertex(vertices, matrix, side ? x0 : x1, y0, side ? z1 : z0, alpha);
        vertex(vertices, matrix, x1, y1, z1, alpha);
        vertex(vertices, matrix, side ? x1 : x0, y1, side ? z0 : z1, alpha);
    }

    private void vertex(VertexConsumer vertices, Matrix4f matrix, float x, float y, float z, int alpha) {
        vertices.addVertex(matrix, x, y, z).setColor(lightColor >> 16 & 255, lightColor >> 8 & 255, lightColor & 255, alpha);
    }

    public void renderTotem(GuiGraphics graphics, float seconds, ItemStack trigger) {
        renderTotem(graphics, seconds, trigger, 0.0F);
    }

    private void renderTotem(GuiGraphics graphics, float seconds, ItemStack trigger, float straighten) {
        if (trigger.isEmpty()) {
            return;
        }
        // 对应原版 GameRenderer.renderItemActivationAnimation 的 40 tick 曲线与 FIXED 物品变换。
        float progress = Math.clamp(seconds / TOTEM_DURATION_SECONDS, 0.0F, TOTEM_CLOSEST_PROGRESS);
        float square = progress * progress;
        float cube = progress * square;
        float curve = 10.25F * cube * square - 24.95F * square * square + 25.5F * cube
                - 13.8F * square + 4.0F * progress;
        float angle = curve * (float) Math.PI;
        float travel = Math.abs((float) Math.sin(angle * 2.0F)) * (1.0F - straighten);
        float size = 50.0F + 175.0F * (float) Math.sin(angle);
        GachaRenderState previousState = new GachaRenderState();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            graphics.flush();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderLights(FRONT_LIGHT, FRONT_FILL_LIGHT);
            pose.translate(graphics.guiWidth() / 2.0F + activationOffsetX * graphics.guiWidth() / 4.0F * travel,
                    graphics.guiHeight() / 2.0F + activationOffsetY * graphics.guiHeight() / 4.0F * travel, SCENE_DEPTH);
            pose.scale(size, -size, size);
            pose.mulPose(Axis.YP.rotationDegrees(900.0F * Math.abs((float) Math.sin(angle)) * (1.0F - straighten)
                    + TOTEM_UPRIGHT_ROTATION * straighten));
            pose.mulPose(Axis.XP.rotationDegrees(6.0F * (float) Math.cos(progress * 8.0F) * (1.0F - straighten)));
            pose.mulPose(Axis.ZP.rotationDegrees(6.0F * (float) Math.cos(progress * 8.0F) * (1.0F - straighten)));
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getItemRenderer().renderStatic(trigger,
                    ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    pose, graphics.bufferSource(), minecraft.level, 0);
            graphics.flush();
        } finally {
            pose.popPose();
            Lighting.setupFor3DItems();
            previousState.close();
        }
    }

    private record PendingSource(@Nullable ChestBlockEntity chest, ItemStack display, Level level,
                                 int previousMenuId, boolean requiresNewMenu, long createdAt) {
    }

    private record ActiveChest(ChestBlockEntity first, @Nullable ChestBlockEntity second, float opening) {
    }
}
