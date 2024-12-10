package net.remmintan.panama.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlockDataProvider;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureRenderInfoProvider;
import net.remmintan.mods.minefortress.core.interfaces.renderers.IGuiBlueprintsRenderer;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.panama.model.BuiltBlueprint;
import net.remmintan.panama.model.BuiltModel;
import net.remmintan.panama.model.builder.BlueprintsModelBuilder;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Supplier;

public final class BlueprintRenderer extends AbstractCustomRenderer implements IGuiBlueprintsRenderer {

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
            final BlockRotation blockRotation = clientBlueprintManager.getSelectedRotation().getRotation();
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

    @Override
    public void renderBlueprintPreview(MatrixStack matrices, String fileName, BlockRotation blockRotation) {
        final BuiltBlueprint builtBlueprint = getBuiltBlueprint(fileName, blockRotation);

        final Vec3i size = builtBlueprint.getSize();
        final int biggestSideSize = Math.max(Math.max(size.getX(), size.getY()), size.getZ());

        final float scale = 80f / biggestSideSize;
        final float scaleFactor = 2f / scale;
        final float x = 130f * scaleFactor;
        final float y = -60f * scaleFactor;
        final float z = 45f * scaleFactor;

        renderBlueprintInGui(matrices, builtBlueprint, scale, x, y, z, true);
    }

    @Override
    public void renderBlueprintInGui(MatrixStack matrices, String blueprintId, BlockRotation blockRotation, int slotColumn, int slotRow, boolean isEnoughResources) {
        renderBlueprintInGui(matrices, blueprintId, blockRotation, 8.5f, -17f, slotColumn, slotRow, isEnoughResources);
    }

    @Override
    public void renderBlueprintInGui(MatrixStack matrices, String blueprintId, BlockRotation blockRotation, float anchorX, float anchorY, int slotColumn, int slotRow, boolean isEnoughResources) {
        final BuiltBlueprint builtBlueprint = getBuiltBlueprint(blueprintId, blockRotation);

        final Vec3i size = builtBlueprint.getSize();
        final int biggestSideSize = Math.max(Math.max(size.getX(), size.getY()), size.getZ());

        final float scale = 11.2f / biggestSideSize;
        final float scaleFactor = 2f/scale;
        final float x = anchorX * scaleFactor + 11.25f * slotColumn * scaleFactor / 1.25f;
        final float y = anchorY * scaleFactor - 11.25f * slotRow * scaleFactor / 1.25f;
        final float z = 22f * scaleFactor;

        renderBlueprintInGui(matrices, builtBlueprint, scale, x, y, z, isEnoughResources);
    }

    public BlueprintsModelBuilder getBlueprintsModelBuilder() {
        return blueprintsModelBuilder;
    }

    private void renderBlueprintInGui(MatrixStack ignoredMatrices, BuiltBlueprint builtBlueprint, float scale, float x, float y, float z, boolean isEnoughResources) {
        super.client.getProfiler().push("blueprint_render_model");
        DiffuseLighting.enableGuiDepthLighting();

        // calculating matrix
        final var projectionMatrix = RenderSystem.getProjectionMatrix();

        final var matrices = RenderSystem.getModelViewStack();
        final var cameraMove = new Vector3f(x, y, z);
        matrices.push();
        rotateScene(matrices, cameraMove);
        matrices.scale(scale, -scale, scale);
        matrices.translate(cameraMove.x(), cameraMove.y(), cameraMove.z());

        this.renderLayer(RenderLayer.getSolid(), builtBlueprint, matrices, projectionMatrix, isEnoughResources);
        this.renderLayer(RenderLayer.getCutout(), builtBlueprint, matrices, projectionMatrix, isEnoughResources);
        this.renderLayer(RenderLayer.getCutoutMipped(), builtBlueprint, matrices, projectionMatrix, isEnoughResources);
        this.renderLayer(RenderLayer.getTranslucent(), builtBlueprint, matrices, projectionMatrix, isEnoughResources);

        matrices.pop();
        super.client.getProfiler().pop();
    }

    @Override
    protected Optional<BuiltModel> getBuiltModel() {
        final BlueprintMetadata selectedStructure = getStructureRenderInfoProvider().getSelectedStructure();
        final var selectedRotation = getStructureRenderInfoProvider().getSelectedRotation();
        final BuiltBlueprint nullableBlueprint = this.blueprintsModelBuilder.getOrBuildBlueprint(selectedStructure.getId(), selectedRotation.getRotation());
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

        ShaderProgram shader = RenderSystem.getShader();
        if(shader == null) throw new IllegalStateException("Shader is null while rendering blueprint");

        for (int i = 0; i < 12; i++) {
            int textureReference = RenderSystem.getShaderTexture(i);
            shader.addSampler("Sampler" + i, textureReference);
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
        if (shader.glintAlpha != null) {
            shader.glintAlpha.set(RenderSystem.getShaderGlintAlpha());
        }
        if(shader.fogStart != null && shader.fogEnd != null && shader.fogColor != null && shader.fogShape != null) {
            shader.fogStart.set(RenderSystem.getShaderFogStart());
            shader.fogEnd.set(RenderSystem.getShaderFogEnd());
            shader.fogColor.set(RenderSystem.getShaderFogColor());
            shader.fogShape.set(0);
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
            if (chunkOffset != null) {
                chunkOffset.set(new Vector3f());
                chunkOffset.upload();
            }

            final VertexBuffer buffer = builtBlueprint.getBuffer(renderLayer);
            buffer.bind();
            buffer.draw();
        }

        if(chunkOffset != null) chunkOffset.set(new Vector3f());
        shader.unbind();

        VertexBuffer.unbind();
        renderLayer.endDrawing();
    }

    @NotNull
    private static IStructureRenderInfoProvider getStructureRenderInfoProvider() {
        final var provider = CoreModUtils.getMineFortressManagersProvider();
        return provider.get_BlueprintManager();
    }

}
