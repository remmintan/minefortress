package org.minefortress.renderer.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;

import java.util.Optional;

public abstract class AbstractCustomRenderer {

    private final MinecraftClient client;

    protected AbstractCustomRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void render(MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f projectionMatrix) {
        if(shouldRender()) {
            renderLayer(RenderLayer.getSolid() ,matrices, cameraX, cameraY, cameraZ, projectionMatrix);
            renderLayer(RenderLayer.getCutout() ,matrices, cameraX, cameraY, cameraZ, projectionMatrix);
            renderLayer(RenderLayer.getCutoutMipped() ,matrices, cameraX, cameraY, cameraZ, projectionMatrix);
        }
    }

    protected void renderLayer(RenderLayer layer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f projectionMatrix) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);

        layer.startDrawing();

        final VertexFormat vertexFormat = layer.getVertexFormat();
        final Shader shader = RenderSystem.getShader();
        if(shader == null) return;

        BufferRenderer.unbindAll();
        int textureReference;
        for (int i = 0; i < 12; i++) {
            textureReference = RenderSystem.getShaderTexture(i);
            shader.addSampler("Sampler" + i, textureReference);
        }
        if(shader.modelViewMat != null) {
            final Matrix4f modelViewMatrix = matrices.peek().getModel();
            shader.modelViewMat.set(modelViewMatrix);
        }
        if(shader.projectionMat != null) {
            shader.projectionMat.set(projectionMatrix);
        }
        if(shader.fogStart != null && shader.fogEnd != null && shader.fogColor != null) {
            shader.fogStart.set(RenderSystem.getShaderFogStart());
            shader.fogEnd.set(RenderSystem.getShaderFogEnd());
            shader.fogColor.set(RenderSystem.getShaderFogColor());
        }
        if(shader.textureMat != null) {
            shader.textureMat.set(RenderSystem.getTextureMatrix());
        }
        if(shader.gameTime != null) {
            shader.gameTime.set(RenderSystem.getShaderGameTime());
        }

        RenderSystem.setupShaderLights(shader);
        shader.bind();

        final GlUniform offset = shader.chunkOffset;

        final Optional<BlockPos> renderTargetPositionOpt = getRenderTargetPosition();
        final Optional<BuiltModel> builtModelOpt = getBuiltModel();

        boolean notEmpty = false;
        if(renderTargetPositionOpt.isPresent() && builtModelOpt.isPresent()) {
            final BlockPos renderTargetPosition = renderTargetPositionOpt.get();
            final BuiltModel builtModel = builtModelOpt.get();

            if(offset != null) {
                Vec3f cameraPosition = new Vec3f((float) cameraX, (float)cameraY, (float)cameraZ);
                final Vec3f targetOffset = new Vec3f(renderTargetPosition.getX(), renderTargetPosition.getY(), renderTargetPosition.getZ());
                targetOffset.subtract(cameraPosition);
                offset.set(targetOffset.getX(), targetOffset.getY(), targetOffset.getZ());
                offset.upload();
            }

            if(builtModel.hasLayer(layer)) {
                final VertexBuffer buffer = builtModel.getBuffer(layer);
                buffer.drawVertices();
                notEmpty = true;
            }
        }

        if(offset != null) {
            offset.set(Vec3f.ZERO);
        }
        shader.unbind();
        if(!notEmpty) {
            vertexFormat.endDrawing();
        }

        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
        layer.endDrawing();
    }

    protected abstract Optional<BlockPos> getRenderTargetPosition();
    protected abstract Optional<BuiltModel> getBuiltModel();
    protected abstract boolean shouldRender();

    protected FortressClientManager getClientManager() {
        return ((FortressMinecraftClient) client).getFortressClientManager();
    }

}
