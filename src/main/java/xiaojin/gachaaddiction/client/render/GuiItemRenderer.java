package xiaojin.gachaaddiction.client.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.GachaConstants;

public class GuiItemRenderer {
    private static final float OPAQUE_ALPHA = 1.0F;
    private static final float GUI_ITEM_SIZE = 16.0F;
    private static final float GUI_ITEM_CENTER = GUI_ITEM_SIZE / 2.0F;
    private static final float GUI_ITEM_DEPTH = 150.0F;
    private static final int MODEL_SEED = 0;
    private static final ModelResourceLocation TRIDENT_GUI_MODEL = ModelResourceLocation.inventory(
            ResourceLocation.withDefaultNamespace("trident"));
    private static final ModelResourceLocation SPYGLASS_GUI_MODEL = ModelResourceLocation.inventory(
            ResourceLocation.withDefaultNamespace("spyglass"));
    private static final String TRANSLUCENT_LAYER_NAME = GachaConstants.MODID + ":gui_item_translucent";
    private static final RenderType TRANSLUCENT_LAYER = RenderType.create(TRANSLUCENT_LAYER_NAME,
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
            Sheets.translucentCullBlockSheet().bufferSize(), true, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderType.CULL)
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setOverlayState(RenderType.OVERLAY)
                    .createCompositeState(true));

    private GuiItemRenderer() {
    }

    public static void renderItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, float alpha) {
        render(guiGraphics, stack, x, y, alpha, false);
    }

    public static void renderFakeItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, float alpha) {
        render(guiGraphics, stack, x, y, alpha, true);
    }

    private static void render(GuiGraphics guiGraphics, ItemStack stack, int x, int y, float alpha,
                               boolean fake) {
        float opacity = Math.clamp(alpha, 0.0F, OPAQUE_ALPHA);
        if (stack.isEmpty() || opacity <= 0.0F) {
            return;
        }
        if (opacity >= OPAQUE_ALPHA) {
            renderNative(guiGraphics, stack, x, y, fake);
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, minecraft.level, fake ? null : minecraft.player,
                MODEL_SEED);
        if (stack.is(Items.TRIDENT)) {
            model = itemRenderer.getItemModelShaper().getModelManager().getModel(TRIDENT_GUI_MODEL);
        } else if (stack.is(Items.SPYGLASS)) {
            model = itemRenderer.getItemModelShaper().getModelManager().getModel(SPYGLASS_GUI_MODEL);
        }
        if (model.isCustomRenderer()) {
            renderNative(guiGraphics, stack, x, y, fake);
            return;
        }

        GachaRenderState previousState = new GachaRenderState();
        float[] shaderColor = RenderSystem.getShaderColor().clone();
        float glintAlpha = RenderSystem.getShaderGlintAlpha();
        PoseStack pose = guiGraphics.pose();
        boolean flatLighting = !model.usesBlockLight();
        pose.pushPose();
        try {
            guiGraphics.flush();
            pose.translate(x + GUI_ITEM_CENTER, y + GUI_ITEM_CENTER, GUI_ITEM_DEPTH);
            pose.scale(GUI_ITEM_SIZE, -GUI_ITEM_SIZE, GUI_ITEM_SIZE);
            if (flatLighting) {
                Lighting.setupForFlatItems();
            }
            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], OPAQUE_ALPHA);
            // Glint uses SRC_COLOR blending, so its intensity is squared during composition.
            RenderSystem.setShaderGlintAlpha(glintAlpha * (float) Math.sqrt(opacity));
            MultiBufferSource buffers = renderType -> new VertexConsumerWrapper(
                    guiGraphics.bufferSource().getBuffer(translucentLayer(renderType))) {
                @Override
                public @NotNull VertexConsumer setColor(int red, int green, int blue, int vertexAlpha) {
                    parent.setColor(red, green, blue, Math.round(vertexAlpha * opacity));
                    return this;
                }
            };
            itemRenderer.render(stack, ItemDisplayContext.GUI, false, pose, buffers,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
            guiGraphics.flush();
        } finally {
            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
            RenderSystem.setShaderGlintAlpha(glintAlpha);
            if (flatLighting) {
                Lighting.setupFor3DItems();
            }
            pose.popPose();
            previousState.close();
        }
    }

    private static void renderNative(GuiGraphics guiGraphics, ItemStack stack, int x, int y, boolean fake) {
        try (GachaRenderState previousState = new GachaRenderState()) {
            if (fake) {
                guiGraphics.renderFakeItem(stack, x, y);
            } else {
                guiGraphics.renderItem(stack, x, y);
            }
        }
    }

    private static RenderType translucentLayer(RenderType renderType) {
        if (renderType == Sheets.solidBlockSheet() || renderType == Sheets.cutoutBlockSheet()
                || renderType == Sheets.translucentCullBlockSheet()
                || renderType == Sheets.translucentItemSheet()
                || renderType == NeoForgeRenderTypes.ITEM_LAYERED_SOLID.get()
                || renderType == NeoForgeRenderTypes.ITEM_LAYERED_CUTOUT.get()
                || renderType == NeoForgeRenderTypes.ITEM_LAYERED_CUTOUT_MIPPED.get()) {
            return TRANSLUCENT_LAYER;
        }
        return renderType;
    }
}
