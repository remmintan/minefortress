package org.minefortress.blueprints;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class BlueprintManager {

    private static final String CURRENT_STRUCTURE = "village/plains/houses/plains_small_house_1";
//    private static final String CURRENT_STRUCTURE = "village/plains/houses/plains_butcher_shop_1";

    private final MinecraftClient client;
    private BlueprintInfo blueprintInfo;

    public BlueprintManager(MinecraftClient client) {
        this.client = client;
    }

    public void buildStructure(ChunkBuilder chunkBuilder) {
        if(blueprintInfo == null) {
            this.blueprintInfo = BlueprintInfo.create(CURRENT_STRUCTURE, client.world, chunkBuilder);
        }

        if(client.crosshairTarget instanceof BlockHitResult blockHitResult)
            this.blueprintInfo.rebuild(blockHitResult.getBlockPos());
    }

    public void renderLayer(RenderLayer renderLayer, MatrixStack matrices, double d, double e, double f, Matrix4f matrix4f) {
        int k;
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        renderLayer.startDrawing();
        this.client.getProfiler().push("filterempty");
        this.client.getProfiler().swap(() -> "render_" + renderLayer);
        boolean g = renderLayer != RenderLayer.getTranslucent();
        VertexFormat h = renderLayer.getVertexFormat();
        Shader shader = RenderSystem.getShader();
        BufferRenderer.unbindAll();
        for (int i = 0; i < 12; ++i) {
            k = RenderSystem.getShaderTexture(i);
            shader.addSampler("Sampler" + i, k);
        }
        if (shader.modelViewMat != null) {
            shader.modelViewMat.set(matrices.peek().getModel());
        }
        if (shader.projectionMat != null) {
            shader.projectionMat.set(matrix4f);
        }
        if (shader.colorModulator != null) {
            shader.colorModulator.set(RenderSystem.getShaderColor());
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
        GlUniform i = shader.chunkOffset;
        k = 0;

        final ChunkBuilder.BuiltChunk chunk = this.blueprintInfo != null ? this.blueprintInfo.getBuiltChunk() : null;
        if(chunk != null && !chunk.getData().isEmpty(renderLayer)) {
            VertexBuffer vertexBuffer = chunk.getBuffer(renderLayer);
            BlockPos blockPos = client.crosshairTarget instanceof BlockHitResult ? ((BlockHitResult) client.crosshairTarget).getBlockPos() : chunk.getOrigin();
            if (i != null) {
                i.set((float)((double)blockPos.getX() - d), (float)((double)blockPos.getY() - e), (float)((double)blockPos.getZ() - f));
                i.upload();
            }
            vertexBuffer.drawVertices();
            k = 1;
        }
        if (i != null) {
            i.set(Vec3f.ZERO);
        }
        shader.unbind();
        if (k != 0) {
            h.endDrawing();
        }
        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
        this.client.getProfiler().pop();
        renderLayer.endDrawing();
    }


    public boolean hasSelectedBlueprint() {
        return true;
    }

}
