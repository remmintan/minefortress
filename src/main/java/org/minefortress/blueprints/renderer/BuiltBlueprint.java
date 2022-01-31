package org.minefortress.blueprints.renderer;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockRenderView;
import org.minefortress.blueprints.BlueprintBlockDataManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BuiltBlueprint {

    private final Set<RenderLayer> initializedLayers = new HashSet<>();
    private final Set<RenderLayer> nonEmptyLayers = new HashSet<>();
    private final Random random = new Random();

    private BufferBuilder.State bufferState;
    private ChunkOcclusionData occlusionData;

    private CompletableFuture<List<Void>> uploadsFuture;

    private final Map<RenderLayer, VertexBuffer> vertexBuffers = RenderLayer
            .getBlockLayers()
            .stream()
            .collect(Collectors.toMap(Function.identity(), it -> new VertexBuffer()));

    private final BlockRenderView blueprintData;
    private final Vec3i size;

    public BuiltBlueprint(BlueprintBlockDataManager.BlueprintBlockData blockData) {
        if(blockData == null) throw new IllegalArgumentException("Block data cannot be null");
        this.blueprintData = new BlueprintBlockRenderView(blockData.getBlueprintData());
        this.size = blockData.getSize();
    }

    public void build(BlockBufferBuilderStorage blockBufferBuilders) {
        render(blockBufferBuilders);
        uploadBuffers(blockBufferBuilders);
    }

    public boolean buffersUploaded() {
        return uploadsFuture.isDone();
    }

    public VertexBuffer getBuffer(RenderLayer layer) {
        return vertexBuffers.get(layer);
    }

    public boolean hasLayer(RenderLayer layer) {
        return this.buffersUploaded() && nonEmptyLayers.contains(layer);
    }

    public Vec3i getSize() {
        return size;
    }

    private void uploadBuffers(BlockBufferBuilderStorage blockBufferBuilders) {
        final List<CompletableFuture<Void>> uploadFutures = initializedLayers
                .stream()
                .map(layer -> {
                    final BufferBuilder bufferBuilder = blockBufferBuilders.get(layer);
                    VertexBuffer vertexBuffer = vertexBuffers.get(layer);

                    return vertexBuffer.submitUpload(bufferBuilder);
                })
                .collect(Collectors.toList());

        uploadsFuture = Util.combine(uploadFutures);
    }


    private void render(BlockBufferBuilderStorage blockBufferBuilders) {
        BlockPos minPos = BlockPos.ORIGIN;
        BlockPos maxPos = minPos.add(15, 15,15);

        MatrixStack matrixStack = new MatrixStack();
        ChunkOcclusionDataBuilder chunkOcclusionDataBuilder = new ChunkOcclusionDataBuilder();

        BlockModelRenderer.enableBrightnessCache();
        final BlockRenderManager blockRenderManager = getClient().getBlockRenderManager();

        for(BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            final BlockState blockState = blueprintData.getBlockState(pos);
            if(blockState.isOpaqueFullCube(blueprintData, pos)) {
                chunkOcclusionDataBuilder.markClosed(pos);
            }
            // TODO: add block entity rendering
            final FluidState fluidState = blueprintData.getFluidState(pos);
            if(!fluidState.isEmpty()) {
                final RenderLayer fluidRenderLayer = RenderLayers.getFluidLayer(fluidState);
                final BufferBuilder bufferBuilder = blockBufferBuilders.get(fluidRenderLayer);
                initLayer(fluidRenderLayer, bufferBuilder);

                if(blockRenderManager.renderFluid(pos, blueprintData, bufferBuilder, fluidState))
                    nonEmptyLayers.add(fluidRenderLayer);

            }

            if(blockState.getRenderType() == BlockRenderType.INVISIBLE) continue;

            final RenderLayer blockLayer = RenderLayers.getBlockLayer(blockState);
            final BufferBuilder bufferBuilder = blockBufferBuilders.get(blockLayer);
            initLayer(blockLayer, bufferBuilder);

            matrixStack.push();
            matrixStack.translate(pos.getX() & 0xf, pos.getY() & 0xf, pos.getZ() & 0xf);

            if(blockRenderManager.renderBlock(blockState, pos, blueprintData, matrixStack, bufferBuilder,  true, random))
                nonEmptyLayers.add(blockLayer);

            matrixStack.pop();
        }
        if(nonEmptyLayers.contains(RenderLayer.getTranslucent())) {
            final BufferBuilder translucentBuilder = blockBufferBuilders.get(RenderLayer.getTranslucent());
            translucentBuilder.setCameraPosition(-minPos.getX(), -minPos.getY(), -minPos.getZ());
            bufferState = translucentBuilder.popState();
        }

        initializedLayers.stream().map(blockBufferBuilders::get).forEach(BufferBuilder::end);
        BlockModelRenderer.disableBrightnessCache();

        occlusionData = chunkOcclusionDataBuilder.build();
    }

    private void initLayer(RenderLayer renderLayer, BufferBuilder bufferBuilder) {
        if(initializedLayers.add(renderLayer))
            beginBufferBuilding(bufferBuilder);
    }


    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    private void beginBufferBuilding(BufferBuilder buffer) {
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
    }

}
