package org.minefortress.blueprints.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Vec3f;
import org.minefortress.interfaces.FortressMinecraftClient;

public final class BlueprintRenderer {

    private final BlueprintsModelBuilder blueprintsModelBuilder;
    private final MinecraftClient client;

    public BlueprintRenderer(MinecraftClient client) {
        blueprintsModelBuilder  = new BlueprintsModelBuilder(
                client.getBufferBuilders(),
                (FortressMinecraftClient) client
        );
        this.client = client;
    }

    public void renderBlueprint(String fileName, BlockRotation rotation, MatrixStack matrices) {
        this.client.getProfiler().push("blueprint_build_model");
        final BuiltBlueprint builtBlueprint = blueprintsModelBuilder.getOrBuildBlueprint(fileName, rotation);
        this.client.getProfiler().pop();
        this.client.getProfiler().push("blueprint_render_model");
        this.renderLayer(RenderLayer.getSolid(), builtBlueprint, matrices);
        this.renderLayer(RenderLayer.getCutout(), builtBlueprint, matrices);
        this.renderLayer(RenderLayer.getCutoutMipped(), builtBlueprint, matrices);
        this.client.getProfiler().pop();
    }

    private void renderLayer(RenderLayer renderLayer, BuiltBlueprint builtBlueprint, MatrixStack matrices) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);

        renderLayer.startDrawing();

        VertexFormat vertexFormat = renderLayer.getVertexFormat();

        Shader shader = RenderSystem.getShader();
        BufferRenderer.unbindAll();
        int k;
        for (int i = 0; i < 12; ++i) {
            k = RenderSystem.getShaderTexture(i);
            shader.addSampler("Sampler" + i, k);
        }
        if (shader.modelViewMat != null) {
            shader.modelViewMat.set(matrices.peek().getModel());
        }
//        if (shader.projectionMat != null) {
//            shader.projectionMat.set(matrix4f);
//        }
        if (shader.colorModulator != null) {
            shader.colorModulator.set(new Vec3f(1F, 1.0F, 1F));
        }
        if (shader.fogStart != null) {
            shader.fogStart.set(RenderSystem.getShaderFogStart());
        }
        if (shader.fogEnd != null) {
            shader.fogEnd.set(RenderSystem.getShaderFogEnd());
        }
        if (shader.fogColor != null) {
            shader.fogColor.set(RenderSystem.getShaderFogColor());
        }
        if (shader.textureMat != null) {
            shader.textureMat.set(RenderSystem.getTextureMatrix());
        }
        if (shader.gameTime != null) {
            shader.gameTime.set(RenderSystem.getShaderGameTime());
        }
        RenderSystem.setupShaderLights(shader);
        shader.bind();

        final GlUniform chunkOffset = shader.chunkOffset;

        final boolean hasLayer = builtBlueprint.hasLayer(renderLayer);
        if(hasLayer) {
            final VertexBuffer vertexBuffer = builtBlueprint.getBuffer(renderLayer);

            if (chunkOffset != null) {
                chunkOffset.set(1f, 1f, 1f);
                chunkOffset.upload();
            }

            vertexBuffer.drawVertices();
        }

        if(chunkOffset != null) chunkOffset.set(Vec3f.ZERO);
        shader.unbind();

        if(hasLayer) vertexFormat.endDrawing();

        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
        renderLayer.endDrawing();
    }

}
