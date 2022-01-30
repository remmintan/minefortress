package org.minefortress.blueprints.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.apache.commons.lang3.builder.Diff;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.CameraTools;

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

    public void renderBlueprint(String fileName, BlockRotation blockRotation, int slotColumn, int slotRow) {
        DiffuseLighting.enableGuiDepthLighting();

        this.client.getProfiler().push("blueprint_build_model");
        final BuiltBlueprint builtBlueprint = blueprintsModelBuilder.getOrBuildBlueprint(fileName, blockRotation);
        this.client.getProfiler().pop();
        this.client.getProfiler().push("blueprint_render_model");

        final float defaultDownscale = 0.0000067f;
        final float defaultYOffset = 20.5f;
        final float defaultXOffset = 49.5f;
        final float defaultZOffset = 8900f;

        final float defaultSlotDelta = 11f;


        final Matrix4f projectionMatrix4f = CameraTools.getProjectionMatrix4f(this.client, 1f).copy();
        final MatrixStack matrices = new MatrixStack();


        float downscale = defaultDownscale;
        matrices.scale(downscale, downscale, downscale);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180f));


        float y = defaultYOffset - defaultSlotDelta * slotRow;
        float x = defaultXOffset - defaultSlotDelta * slotColumn;

        final Vec3f moveVector = new Vec3f(x, y, defaultZOffset);

        rotateBlueprint(matrices, moveVector, -45f, -30f);
        matrices.translate(moveVector.getX(), moveVector.getY(), moveVector.getZ());

        this.renderLayer(RenderLayer.getSolid(), builtBlueprint, matrices, projectionMatrix4f);
        this.renderLayer(RenderLayer.getCutout(), builtBlueprint, matrices, projectionMatrix4f);
        this.renderLayer(RenderLayer.getCutoutMipped(), builtBlueprint, matrices, projectionMatrix4f);


        this.client.getProfiler().pop();
    }

    private void rotateBlueprint(MatrixStack matrices, Vec3f moveVector, float yawRotation, float pitchRotation) {
        final Vec3f yawSceneRotationAxis = Vec3f.POSITIVE_Y;
        final Vec3f yawMoveRotationAxis = Vec3f.NEGATIVE_Y;
        final Quaternion yawSceneRotation = yawSceneRotationAxis.getDegreesQuaternion(yawRotation);
        final Quaternion yawMoveRotation = yawMoveRotationAxis.getDegreesQuaternion(yawRotation);
        matrices.multiply(yawSceneRotation);
        moveVector.rotate(yawMoveRotation);

        final Vec3f pitchSceneRotationAxis = Vec3f.POSITIVE_X.copy();
        final Vec3f pitchMoveRotationAxis = Vec3f.NEGATIVE_X.copy();
        pitchSceneRotationAxis.rotate(yawMoveRotation);
        pitchMoveRotationAxis.rotate(yawMoveRotation);
        final Quaternion pitchSceneRotation = pitchSceneRotationAxis.getDegreesQuaternion(pitchRotation);
        final Quaternion pitchMoveRotation = pitchMoveRotationAxis.getDegreesQuaternion(pitchRotation);
        matrices.multiply(pitchSceneRotation);
        moveVector.rotate(pitchMoveRotation);
    }

    private void renderLayer(RenderLayer renderLayer, BuiltBlueprint builtBlueprint, MatrixStack matrices, Matrix4f matrix4f) {
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
        if (shader.projectionMat != null) {
            shader.projectionMat.set(matrix4f);
        }
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
                chunkOffset.set(Vec3f.ZERO);
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
