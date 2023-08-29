package net.remmintan.panama.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.remmintan.panama.model.BuiltModel;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class AbstractCustomRenderer {

    protected final MinecraftClient client;

    protected AbstractCustomRenderer(MinecraftClient client) {
        this.client = client;
    }

    public void render(MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f projectionMatrix) {
        if(shouldRender()) {
            for(RenderLayer layer : getRenderLayers()) {
                renderLayer(layer, matrices, cameraX, cameraY, cameraZ, projectionMatrix);
            }
        }
    }

    public void renderTranslucent(MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f projectionMatrix) {
        if(shouldRender()) {
            renderLayer(RenderLayer.getTranslucent(), matrices, cameraX, cameraY, cameraZ, projectionMatrix);
        }
    }

    protected List<RenderLayer> getRenderLayers() {
        return Arrays.asList(RenderLayer.getSolid(), RenderLayer.getCutout(), RenderLayer.getCutoutMipped());
    }

    protected void renderLayer(RenderLayer layer, MatrixStack matrices, double cameraX, double cameraY, double cameraZ, Matrix4f projectionMatrix) {
        final Optional<BuiltModel> builtModelOpt = getBuiltModel();
        if(!builtModelOpt.map(it -> it.hasLayer(layer)).orElse(false)) return;

        RenderSystem.assertOnRenderThread();

        layer.startDrawing();

        final ShaderProgram shader = RenderSystem.getShader();
        if(shader == null) throw new IllegalStateException("Shader is null");


        for (int i = 0; i < 12; i++) {
            int textureReference = RenderSystem.getShaderTexture(i);
            shader.addSampler("Sampler" + i, textureReference);
        }
        if(shader.modelViewMat != null) {
            matrices.push();
            Matrix4f modelViewMatrix = matrices.peek().getPositionMatrix();
            modelViewMatrix.translate((float)-cameraX, (float)-cameraY, (float)-cameraZ);
            shader.modelViewMat.set(modelViewMatrix);
            matrices.pop();
        }

        if(shader.projectionMat != null) {
            shader.projectionMat.set(projectionMatrix);
        }
        if (shader.colorModulator != null) {
            shader.colorModulator.set(getColorModulator());
        }
        if (shader.glintAlpha != null) {
            shader.glintAlpha.set(RenderSystem.getShaderGlintAlpha());
        }
        if(shader.fogStart != null && shader.fogEnd != null && shader.fogColor != null && shader.fogShape != null) {
            shader.fogStart.set(RenderSystem.getShaderFogStart());
            shader.fogEnd.set(RenderSystem.getShaderFogEnd());
            shader.fogColor.set(RenderSystem.getShaderFogColor());
            shader.fogShape.set(0);
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
        if(renderTargetPositionOpt.isPresent()) {
            final BlockPos renderTargetPosition = renderTargetPositionOpt.get();
            final BuiltModel builtModel = builtModelOpt.get();

            if(offset != null) {
                final Vector3f targetOffset = new Vector3f(renderTargetPosition.getX(), renderTargetPosition.getY(), renderTargetPosition.getZ());
                offset.set(targetOffset);
                offset.upload();
            }

            final VertexBuffer buffer = builtModel.getBuffer(layer);
            buffer.bind();
            buffer.draw();
        }

        if(offset != null) {
            offset.set(new Vector3f());
        }
        shader.unbind();

        VertexBuffer.unbind();
        layer.endDrawing();
    }

    protected Optional<BlockPos> getRenderTargetPosition() {
        if(shouldRender()) {
            return Optional.of(BlockPos.ORIGIN);
        }
        return Optional.empty();
    }
    protected abstract Optional<BuiltModel> getBuiltModel();
    protected abstract boolean shouldRender();
    public abstract void prepareForRender();
    public abstract void close();

    protected Vector3f getColorModulator() {
        return new Vector3f(1f, 1f, 1f);
    }

    protected FortressClientManager getClientManager() {
        return ((FortressMinecraftClient) client).get_FortressClientManager();
    }

}
