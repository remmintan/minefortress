package org.minefortress.blueprints.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.*;
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

    public void renderBlueprint(String fileName, BlockRotation blockRotation, int slotColumn, int slotRow) {
        DiffuseLighting.enableGuiDepthLighting();

        this.client.getProfiler().push("blueprint_build_model");
        final BuiltBlueprint builtBlueprint = blueprintsModelBuilder.getOrBuildBlueprint(fileName, blockRotation);
        this.client.getProfiler().pop();
        this.client.getProfiler().push("blueprint_render_model");

        // calculating matrix
        final MatrixStack matrices = RenderSystem.getModelViewStack();
        final Matrix4f projectionMatrix4f = RenderSystem.getProjectionMatrix();

        final Vec3i size = builtBlueprint.getSize();

        final int biggestSideSize = Math.max(Math.max(size.getX(), size.getY()), size.getZ());

        final float scale = 1.6f * 7 / biggestSideSize;
        final float scaleFactor = 2f/scale;
        final float x = 8.5f * scaleFactor + 11.25f * slotColumn * scaleFactor / 1.25f;
        final float y = -17f * scaleFactor - 11.25f * slotRow  * scaleFactor / 1.25f;
        final Vec3f cameraMove = new Vec3f(x, y, 22f*scaleFactor);
        matrices.push();

        rotateScene(matrices, cameraMove);
        matrices.scale(scale, -scale, scale);
        matrices.translate(cameraMove.getX(), cameraMove.getY(), cameraMove.getZ());


        this.renderLayer(RenderLayer.getSolid(), builtBlueprint, matrices, projectionMatrix4f);
        this.renderLayer(RenderLayer.getCutout(), builtBlueprint, matrices, projectionMatrix4f);
        this.renderLayer(RenderLayer.getCutoutMipped(), builtBlueprint, matrices, projectionMatrix4f);

        matrices.pop();

        this.client.getProfiler().pop();
    }

    private void rotateScene(MatrixStack matrices, Vec3f cameraMove) {
        final float yaw = 135f;
        final float pitch = -30f;

        // calculating rotations
        final Vec3f yawSceneRotationAxis = Vec3f.POSITIVE_Y;
        final Vec3f yawMoveRotationAxis = Vec3f.NEGATIVE_Y;
        final Quaternion yawSceneRotation = yawSceneRotationAxis.getDegreesQuaternion(yaw);
        final Quaternion yawMoveRotation = yawMoveRotationAxis.getDegreesQuaternion(yaw);

        final Vec3f pitchSceneRotationAxis = Vec3f.POSITIVE_X.copy();
        final Vec3f pitchMoveRotationAxis = Vec3f.POSITIVE_X.copy();
        pitchSceneRotationAxis.rotate(yawMoveRotation);
        pitchMoveRotationAxis.rotate(yawMoveRotation);
        final Quaternion pitchSceneRotation = pitchSceneRotationAxis.getDegreesQuaternion(pitch);
        final Quaternion pitchMoveRotation = pitchMoveRotationAxis.getDegreesQuaternion(pitch);

        // rotating camera
        cameraMove.rotate(yawMoveRotation);
        cameraMove.rotate(pitchMoveRotation);

        matrices.multiply(yawSceneRotation);
        matrices.multiply(pitchSceneRotation);
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
