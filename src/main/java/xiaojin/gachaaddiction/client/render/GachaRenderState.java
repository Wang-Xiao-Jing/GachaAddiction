package xiaojin.gachaaddiction.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.neoforge.client.GlStateBackup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GachaRenderState implements AutoCloseable {
    // 与1.21.1的ShaderInstance.setDefaultUniforms使用的采样槽数量一致。
    private static final int SHADER_TEXTURE_SLOTS = 12;
    private final GlStateBackup glState = new GlStateBackup();
    private final @Nullable ShaderInstance shader;
    private final float[] color;
    private final float glintAlpha;
    private final Matrix4f projection;
    private final Matrix4f modelView;
    private final Matrix4f textureMatrix;
    private final VertexSorting sorting;
    private final int[] textures = new int[SHADER_TEXTURE_SLOTS];

    public GachaRenderState() {
        RenderSystem.backupGlState(glState);
        shader = RenderSystem.getShader();
        color = RenderSystem.getShaderColor().clone();
        glintAlpha = RenderSystem.getShaderGlintAlpha();
        projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        modelView = new Matrix4f(RenderSystem.getModelViewStack());
        textureMatrix = new Matrix4f(RenderSystem.getTextureMatrix());
        sorting = RenderSystem.getVertexSorting();
        for (int slot = 0; slot < textures.length; slot++) {
            textures[slot] = RenderSystem.getShaderTexture(slot);
        }
    }

    @Override
    public void close() {
        try {
            // EffectInstance与ShaderInstance分别缓存程序，后处理退出时需使普通着色器缓存失效。
            ShaderInstance current = RenderSystem.getShader();
            if (current != null) {
                current.clear();
            }
            RenderSystem.setProjectionMatrix(projection, sorting);
            RenderSystem.getModelViewStack().set(modelView);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setTextureMatrix(textureMatrix);
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            RenderSystem.setShaderGlintAlpha(glintAlpha);
            for (int slot = 0; slot < textures.length; slot++) {
                RenderSystem.setShaderTexture(slot, textures[slot]);
            }
            // 原版在下一次绘制时设置uniform并绑定程序，此处只恢复逻辑引用。
            RenderSystem.setShader(() -> shader);
        } finally {
            RenderSystem.restoreGlState(glState);
        }
    }
}
