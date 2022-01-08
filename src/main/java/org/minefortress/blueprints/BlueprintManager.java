package org.minefortress.blueprints;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.network.ServerboundBlueprintTaskPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.tasks.BuildingManager;

import java.util.*;
import java.util.stream.Collectors;

public class BlueprintManager {

    private static final String CURRENT_STRUCTURE = "village/plains/houses/plains_small_house_1";
//    private static final String CURRENT_STRUCTURE = "village/plains/houses/plains_butcher_shop_1";

    private final MinecraftClient client;

    private StructureInfo selectedStructure;
    private final Map<String, BlueprintInfo> blueprintInfos = new HashMap<>();

    public BlueprintManager(MinecraftClient client) {
        this.client = client;
    }

    public void buildStructure(ChunkBuilder chunkBuilder) {
        final String selectedStructureName = selectedStructure.fileId();
        if(!blueprintInfos.containsKey(selectedStructureName)) {
            final BlueprintInfo blueprintInfo = BlueprintInfo.create(selectedStructureName, client.world, chunkBuilder);
            blueprintInfos.put(selectedStructureName, blueprintInfo);
        }

        if(client.crosshairTarget instanceof BlockHitResult blockHitResult) {
            final BlockPos blockPos = blockHitResult.getBlockPos();
            if(blockPos != null)
                this.blueprintInfos.get(selectedStructureName).rebuild(blockPos);
        }
    }

    public void renderBlockEntities(VertexConsumerProvider.Immediate immediate, MatrixStack matrices, Vec3d cameraPos, BlockEntityRenderDispatcher blockEntityRenderDispatcher, float tickDelta) {
        BlockPos selectedPos = getSelectedPos();
        if(selectedPos == null) return;
        final ChunkBuilder.BuiltChunk builtChunk = getBuiltChunk();
        if(builtChunk == null) return;

//        immediate.draw(RenderLayer.getEntitySolid(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
//        immediate.draw(RenderLayer.getEntityCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
//        immediate.draw(RenderLayer.getEntityCutoutNoCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
//        immediate.draw(RenderLayer.getEntitySmoothCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));

        for (BlockEntity entity : builtChunk.getData().getBlockEntities()) {
            BlockPos r = entity.getPos();
            matrices.push();
            matrices.translate((double)r.getX() - cameraPos.x, (double)r.getY() - cameraPos.y, (double)r.getZ() - cameraPos.z);
            blockEntityRenderDispatcher.render(entity, tickDelta, matrices, immediate);
            matrices.pop();
        }

        immediate.draw(TexturedRenderLayers.getEntitySolid());
        immediate.draw(TexturedRenderLayers.getEntityCutout());
        immediate.draw(TexturedRenderLayers.getBeds());
        immediate.draw(TexturedRenderLayers.getShulkerBoxes());
        immediate.draw(TexturedRenderLayers.getSign());
        immediate.draw(TexturedRenderLayers.getChest());
    }

    @Nullable
    private BlockPos getSelectedPos() {
        if(client.crosshairTarget instanceof BlockHitResult) {
            final BlockPos originalPos = ((BlockHitResult) client.crosshairTarget).getBlockPos();
            return moveToStructureSize(originalPos);
        } else {
            return null;
        }
    }

    private BlockPos moveToStructureSize(BlockPos pos) {
        final Vec3i size = blueprintInfos.get(selectedStructure.fileId()).getSize();
        final Vec3i halfSize = new Vec3i(size.getX() / 2, 0, size.getZ() / 2);
        final BlockPos movedPos = pos.subtract(halfSize);
        final boolean movedPosSolid = !BuildingManager.doesNotHaveCollisions(client.world, movedPos);

        return movedPosSolid?movedPos.up():movedPos;
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

        final ChunkBuilder.BuiltChunk chunk = getBuiltChunk();
        BlockPos blockPos = getSelectedPos();
        if(chunk != null && !chunk.getData().isEmpty(renderLayer) && blockPos != null) {
            VertexBuffer vertexBuffer = chunk.getBuffer(renderLayer);
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

    @Nullable
    private ChunkBuilder.BuiltChunk getBuiltChunk() {
        return this.blueprintInfos.get(this.selectedStructure.fileId()).getBuiltChunk();
    }


    public boolean hasSelectedBlueprint() {
        return selectedStructure != null;
    }

    public void selectStructure(StructureInfo structureInfo) {
        this.selectedStructure = structureInfo;
    }

    public void buildCurrentStructure(BlockPos clickPos) {
        if(selectedStructure == null) throw new IllegalStateException("No blueprint selected");

        final BlockPos startPos = moveToStructureSize(clickPos);

        UUID taskId = UUID.randomUUID();
        final FortressClientWorld world = (FortressClientWorld) client.world;
        if(world != null) {
            final Map<BlockPos, BlockState> structureData = blueprintInfos.get(selectedStructure.fileId()).getChunkRendererRegion().getStructureData();
            final List<BlockPos> blocks = structureData
                    .entrySet()
                    .stream()
                    .filter(ent -> ent.getValue().getBlock() != Blocks.AIR)
                    .map(Map.Entry::getKey)
                    .map(it -> it.add(startPos))
                    .collect(Collectors.toList());
            world.getClientTasksHolder().addTask(taskId, blocks);
        }
        final ServerboundBlueprintTaskPacket serverboundBlueprintTaskPacket = new ServerboundBlueprintTaskPacket(taskId, selectedStructure.fileId(), startPos);
        FortressClientNetworkHelper.send(FortressChannelNames.NEW_BLUEPRINT_TASK, serverboundBlueprintTaskPacket);
    }

    public void clearStructure() {
        this.selectedStructure = null;
    }

}
