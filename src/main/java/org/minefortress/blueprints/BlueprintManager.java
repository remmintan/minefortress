package org.minefortress.blueprints;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.renderer.BlueprintRenderer;
import org.minefortress.blueprints.renderer.BlueprintsModelBuilder;
import org.minefortress.blueprints.renderer.BuiltBlueprint;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.ServerboundBlueprintTaskPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.tasks.BuildingManager;

import java.util.*;
import java.util.stream.Collectors;

public class BlueprintManager {

    private static final Vec3f WRONG_PLACEMENT_COLOR = new Vec3f(1.0F, 0.5F, 0.5F);
    private static final Vec3f CORRECT_PLACEMENT_COLOR = new Vec3f(1F, 1.0F, 1F);

    private final MinecraftClient client;
    private final BlueprintBlockDataManager blockDataManager;
    private final BlueprintsModelBuilder blueprintsBuilder;

    private BlueprintMetadata selectedStructure;
    private BlockPos blueprintBuildPos = null;
    private boolean cantBuild = false;

    public BlueprintManager(MinecraftClient client) {
        this.client = client;
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
        this.blockDataManager = fortressClient.getBlueprintBlockDataManager();
        this.blueprintsBuilder = fortressClient.getBlueprintRenderer().getBlueprintsModelBuilder();
    }

    public void buildStructure() {
        final String file = selectedStructure.getFile();
        final BlockRotation rotation = selectedStructure.getRotation();
        this.blueprintsBuilder.buildBlueprint(file, rotation);

    }

    public void tick() {
        if(!hasSelectedBlueprint()) return;
        blueprintBuildPos = getSelectedPos();
        if(blueprintBuildPos == null) return;
        checkCantBuild();
    }

    private void checkCantBuild() {
        final BlueprintBlockDataManager.BlueprintBlockData blockData = blockDataManager
                .getBlockData(selectedStructure.getFile(), selectedStructure.getRotation(), false);
        final Set<BlockPos> blueprintDataPositions = blockData.getBlueprintData().keySet();
        final boolean blueprintPartInTheSurface = blueprintDataPositions.stream()
                .filter(blockPos -> !(blockData.isStandsOnGrass() && blockPos.getY() == 0))
                .map(pos -> pos.add(blueprintBuildPos))
                .anyMatch(pos -> !BuildingManager.canPlaceBlock(client.world, pos));

        final boolean blueprintPartInTheAir = blueprintDataPositions.stream()
                .filter(blockPos -> {
                    if (blockData.isStandsOnGrass()) {
                        return blockPos.getY() == 1 && !blueprintDataPositions.contains(blockPos.down());
                    } else {
                        return blockPos.getY() == 0;
                    }
                })
                .map(pos -> pos.add(blueprintBuildPos))
                .anyMatch(pos -> BuildingManager.canPlaceBlock(client.world, pos.down()));

        cantBuild = blueprintPartInTheSurface || blueprintPartInTheAir;
    }

    @Nullable
    private BlockPos getSelectedPos() {
        if(client.crosshairTarget instanceof BlockHitResult) {
            final BlockPos originalPos = ((BlockHitResult) client.crosshairTarget).getBlockPos();
            if(originalPos != null) return moveToStructureSize(originalPos);
        }
        return null;
    }

    private BlockPos moveToStructureSize(BlockPos pos) {
        if(selectedStructure == null) return pos;

        final boolean posSolid = !BuildingManager.doesNotHaveCollisions(client.world, pos);
        final BlueprintBlockDataManager.BlueprintBlockData blockData = blockDataManager
                .getBlockData(selectedStructure.getFile(), selectedStructure.getRotation(), false);
        final Vec3i size = blockData.getSize();
        final Vec3i halfSize = new Vec3i(size.getX() / 2, 0, size.getZ() / 2);
        BlockPos movedPos = pos.subtract(halfSize);
        movedPos = blockData.isStandsOnGrass() ? movedPos.down() : movedPos;
        movedPos = posSolid? movedPos.up():movedPos;
        return movedPos;
    }

    public void renderLayer(RenderLayer renderLayer, MatrixStack matrices, double d, double e, double f, Matrix4f matrix4f) {
        int k;
        RenderSystem.assertOnRenderThread();
        renderLayer.startDrawing();
        this.client.getProfiler().push("filterempty");
        this.client.getProfiler().swap(() -> "render_" + renderLayer);
        VertexFormat h = renderLayer.getVertexFormat();
        Shader shader = RenderSystem.getShader();
        BufferRenderer.unbindAll();
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
            shader.colorModulator.set(cantBuild ? WRONG_PLACEMENT_COLOR : CORRECT_PLACEMENT_COLOR);
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

        final BuiltBlueprint chunk = getBuiltBlueprint();
        if(chunk != null && chunk.buffersUploaded() && chunk.hasLayer(renderLayer) && blueprintBuildPos != null) {
            VertexBuffer vertexBuffer = chunk.getBuffer(renderLayer);
            if (i != null) {
                i.set((float)((double)blueprintBuildPos.getX() - d), (float)((double)blueprintBuildPos.getY() - e), (float)((double)blueprintBuildPos.getZ() - f));
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

    @Nullable
    private BuiltBlueprint getBuiltBlueprint() {
        return this.blueprintsBuilder.getOrBuildBlueprint(this.selectedStructure.getFile(), this.selectedStructure.getRotation());
    }


    public boolean hasSelectedBlueprint() {
        return selectedStructure != null;
    }

    public void selectStructure(BlueprintMetadata blueprintMetadata) {
        this.selectedStructure = blueprintMetadata;
    }

    public void buildCurrentStructure(BlockPos clickPos) {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");
        if(blueprintBuildPos == null) throw new IllegalStateException("No blueprint build position");

        if(cantBuild) return;

        UUID taskId = UUID.randomUUID();
        final FortressClientWorld world = (FortressClientWorld) client.world;
        if(world != null) {
            final Map<BlockPos, BlockState> structureData = blockDataManager
                    .getBlockData(selectedStructure.getFile(), selectedStructure.getRotation(), false)
                    .getBlueprintData();
            final List<BlockPos> blocks = structureData
                    .keySet()
                    .stream()
                    .map(it -> it.add(blueprintBuildPos))
                    .collect(Collectors.toList());
            world.getClientTasksHolder().addTask(taskId, blocks);
        }
        final ServerboundBlueprintTaskPacket serverboundBlueprintTaskPacket = new ServerboundBlueprintTaskPacket(taskId, selectedStructure.getId(), selectedStructure.getFile(), blueprintBuildPos, selectedStructure.getRotation());
        FortressClientNetworkHelper.send(FortressChannelNames.NEW_BLUEPRINT_TASK, serverboundBlueprintTaskPacket);

        if(!client.options.keySprint.isPressed()) {
            clearStructure();
        }
    }

    public void clearStructure() {
        this.selectedStructure = null;
    }

    public String getSelectedStructureName() {
        return this.selectedStructure != null ? this.selectedStructure.getName() : "";
    }

    public void rotateSelectedStructureClockwise() {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");
        this.selectedStructure.rotateRight();
    }

    public void rotateSelectedStructureCounterClockwise() {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");
        this.selectedStructure.rotateLeft();
    }

}
