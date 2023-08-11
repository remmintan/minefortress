package org.minefortress.blueprints.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.minefortress.blueprints.interfaces.IBlockDataProvider;
import org.minefortress.blueprints.interfaces.IStructureRenderInfoProvider;
import org.minefortress.blueprints.manager.BlueprintMetadata;
import org.minefortress.fortress.FortressState;
import org.minefortress.renderer.custom.AbstractCustomRenderer;
import org.minefortress.renderer.custom.BuiltModel;
import org.minefortress.utils.ModUtils;

import java.util.Optional;
import java.util.function.Supplier;

public final class BlueprintRenderer extends AbstractCustomRenderer {

    private static final Vector3f WRONG_PLACEMENT_COLOR = new Vector3f(1.0F, 0.5F, 0.5F);
    private static final Vector3f CORRECT_PLACEMENT_COLOR = new Vector3f(1F, 1.0F, 1F);

    private final BlueprintsModelBuilder blueprintsModelBuilder;

    public BlueprintRenderer(Supplier<IBlockDataProvider> blockDataProviderSupplier, MinecraftClient client, BlockBufferBuilderStorage blockBufferBuilderStorage) {
        super(client);
        blueprintsModelBuilder  = new BlueprintsModelBuilder(blockBufferBuilderStorage, blockDataProviderSupplier);
    }

    @Override
    public void prepareForRender() {
        final IStructureRenderInfoProvider clientBlueprintManager = getStructureRenderInfoProvider();
        if(clientBlueprintManager.isSelecting()) {
            final BlueprintMetadata selectedStructure = clientBlueprintManager.getSelectedStructure();
            final BlockRotation blockRotation = selectedStructure.getRotation();
            final String fileName = selectedStructure.getId();
            blueprintsModelBuilder.getOrBuildBlueprint(fileName, blockRotation);
        }
    }

    @Override
    public void close() {
        blueprintsModelBuilder.reset();
    }

    @Override
    protected boolean shouldRender() {
        return getStructureRenderInfoProvider().isSelecting();
    }

    @Override
    protected Vector3f getColorModulator() {
        return getStructureRenderInfoProvider().canBuild() ? CORRECT_PLACEMENT_COLOR : WRONG_PLACEMENT_COLOR;
    }

    @Override
    protected Optional<BlockPos> getRenderTargetPosition() {
        return getStructureRenderInfoProvider().getStructureRenderPos();
    }

    public void renderBlueprintPreview(String fileName, BlockRotation blockRotation) {
        final BuiltBlueprint builtBlueprint = getBuiltBlueprint(fileName, blockRotation);

        final Vec3i size = builtBlueprint.getSize();
        final int biggestSideSize = Math.max(Math.max(size.getX(), size.getY()), size.getZ());

        final float scale = 80f / biggestSideSize;
        final float scaleFactor = 2f / scale;
        final float x = 130f * scaleFactor;
        final float y = -60f * scaleFactor;
        final float z = 45f * scaleFactor;

        renderBlueprintInGui(builtBlueprint, scale, x, y, z, true);
    }

    public void renderBlueprintInGui(String fileName, BlockRotation blockRotation, int slotColumn, int slotRow, boolean isEnoughResources) {
        final BuiltBlueprint builtBlueprint = getBuiltBlueprint(fileName, blockRotation);

        final Vec3i size = builtBlueprint.getSize();
        final int biggestSideSize = Math.max(Math.max(size.getX(), size.getY()), size.getZ());

        final float scale = 1.6f * 7 / biggestSideSize;
        final float scaleFactor = 2f/scale;
        final float x = 8.5f * scaleFactor + 11.25f * slotColumn * scaleFactor / 1.25f;
        final float y = -17f * scaleFactor - 11.25f * slotRow  * scaleFactor / 1.25f;
        final float z = 22f * scaleFactor;

        renderBlueprintInGui(builtBlueprint, scale, x, y, z, isEnoughResources);
    }

    public BlueprintsModelBuilder getBlueprintsModelBuilder() {
        return blueprintsModelBuilder;
    }

    private void renderBlueprintInGui(BuiltBlueprint builtBlueprint, float scale, float x, float y, float z, boolean isEnoughResources) {
        super.client.getProfiler().push("blueprint_render_model");
        DiffuseLighting.enableGuiDepthLighting();

        // calculating matrix
        final MatrixStack matrices = RenderSystem.getModelViewStack();
        final Matrix4f projectionMatrix4f = RenderSystem.getProjectionMatrix();

        final Vector3f cameraMove = new Vector3f(x, y, z);
        matrices.push();

        rotateScene(matrices, cameraMove);
        matrices.scale(scale, -scale, scale);
        matrices.translate(cameraMove.x(), cameraMove.y(), cameraMove.z());

        this.renderLayer(RenderLayer.getSolid(), builtBlueprint, matrices, projectionMatrix4f, isEnoughResources);
        this.renderLayer(RenderLayer.getCutout(), builtBlueprint, matrices, projectionMatrix4f, isEnoughResources);
        this.renderLayer(RenderLayer.getCutoutMipped(), builtBlueprint, matrices, projectionMatrix4f, isEnoughResources);
        this.renderLayer(RenderLayer.getTranslucent(), builtBlueprint, matrices, projectionMatrix4f, isEnoughResources);

        matrices.pop();
        super.client.getProfiler().pop();
    }

    @Override
    protected Optional<BuiltModel> getBuiltModel() {
        final BlueprintMetadata selectedStructure = getStructureRenderInfoProvider().getSelectedStructure();
        final BuiltBlueprint nullableBlueprint = this.blueprintsModelBuilder.getOrBuildBlueprint(selectedStructure.getId(), selectedStructure.getRotation());
        return Optional.ofNullable(nullableBlueprint);
    }

    private BuiltBlueprint getBuiltBlueprint(String fileName, BlockRotation blockRotation) {
        this.client.getProfiler().push("blueprint_build_model");
        final BuiltBlueprint builtBlueprint = blueprintsModelBuilder.getOrBuildBlueprint(fileName, blockRotation);
        this.client.getProfiler().pop();

        return builtBlueprint;
    }

    private void rotateScene(MatrixStack matrices, Vector3f cameraMove) {
        final var yaw = 135f;
        final var pitch = -30f;

        final var radiansYaw = (float)Math.toRadians(yaw);
        final var radiansPitch = (float)Math.toRadians(pitch);

        // calculating rotations
        final var yawSceneRotation = new Quaternionf().set(new AxisAngle4f(radiansYaw, 0, 1, 0));
        final var yawMoveRotation = new Quaternionf().set(new AxisAngle4f(radiansYaw, 0, -1, 0));

        final var pitchSceneRotationAxis = new Vector3f(1, 0, 0).rotate(yawMoveRotation);
        final var pitchMoveRotationAxis = new Vector3f(1, 0, 0).rotate(yawMoveRotation);
        final var pitchSceneRotation = new Quaternionf().set(new AxisAngle4f(radiansPitch, pitchSceneRotationAxis));
        final var pitchMoveRotation = new Quaternionf().set(new AxisAngle4f(radiansPitch, pitchMoveRotationAxis));

        // rotating camera
        cameraMove.rotate(yawMoveRotation);
        cameraMove.rotate(pitchMoveRotation);

        matrices.multiply(yawSceneRotation);
        matrices.multiply(pitchSceneRotation);
    }

    private void renderLayer(RenderLayer renderLayer, BuiltBlueprint builtBlueprint, MatrixStack matrices, Matrix4f matrix4f, boolean isEnoughResources) {
        RenderSystem.assertOnRenderThread();

        renderLayer.startDrawing();

        VertexFormat vertexFormat = renderLayer.getVertexFormat();

        ShaderProgram shader = RenderSystem.getShader();
        if(shader == null) throw new IllegalStateException("Shader is null while rendering blueprint");
        BufferRenderer.reset();
        int k;
        for (int i = 0; i < 12; ++i) {
            k = RenderSystem.getShaderTexture(i);
            shader.addSampler("Sampler" + i, k);
        }
        if (shader.modelViewMat != null) {
            shader.modelViewMat.set(matrices.peek().getPositionMatrix());
        }
        if (shader.projectionMat != null) {
            shader.projectionMat.set(matrix4f);
        }
        if (shader.colorModulator != null) {
            shader.colorModulator.set(isEnoughResources?CORRECT_PLACEMENT_COLOR:WRONG_PLACEMENT_COLOR);
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
                chunkOffset.set(new Vector3f(0));
                chunkOffset.upload();
            }

            vertexBuffer.draw();
        }

        if(chunkOffset != null) chunkOffset.set(new Vector3f(0));
        shader.unbind();

        if(hasLayer) vertexFormat.clearState();

        VertexBuffer.unbind();
        renderLayer.endDrawing();
    }

    @NotNull
    private static IStructureRenderInfoProvider getStructureRenderInfoProvider() {
        final var state = ModUtils.getFortressClientManager().getState();
        if(state == FortressState.COMBAT) {
            return ModUtils.getInfluenceManager();
        }
        return ModUtils.getBlueprintManager();
    }

}
