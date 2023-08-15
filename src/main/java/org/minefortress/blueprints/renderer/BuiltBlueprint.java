package org.minefortress.blueprints.renderer;

import com.mojang.blaze3d.systems.VertexSorter;
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
import net.minecraft.world.biome.ColorResolver;
import org.minefortress.blueprints.data.StrctureBlockData;
import org.minefortress.blueprints.data.BlueprintDataLayer;
import org.minefortress.renderer.custom.BuiltModel;
import org.minefortress.selections.renderer.RenderHelper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BuiltBlueprint implements BuiltModel {

    private final Set<RenderLayer> initializedLayers = new HashSet<>();
    private final Set<RenderLayer> nonEmptyLayers = new HashSet<>();
    private final Random random = new Random();

    private BufferBuilder.TransparentSortingData bufferState;
    private ChunkOcclusionData occlusionData;

    private CompletableFuture<List<Void>> uploadsFuture;

    private final Map<RenderLayer, VertexBuffer> vertexBuffers = RenderLayer
            .getBlockLayers()
            .stream()
            .collect(Collectors.toMap(Function.identity(), it -> new VertexBuffer(VertexBuffer.Usage.STATIC)));

    private final BlockRenderView blueprintData;
    private final Vec3i size;

    public BuiltBlueprint(StrctureBlockData blockData, BiFunction<BlockState, ColorResolver, Integer> colorProvider) {
        if(blockData == null) throw new IllegalArgumentException("Block data cannot be null");
        if(!blockData.hasLayer(BlueprintDataLayer.GENERAL)) throw new IllegalArgumentException("Block data must have a general layer");
        this.blueprintData = new BlueprintBlockRenderView(blockData.getLayer(BlueprintDataLayer.GENERAL), colorProvider);
        this.size = blockData.getSize();
    }

    public void build(BlockBufferBuilderStorage blockBufferBuilders) {
        render(blockBufferBuilders);
        uploadBuffers(blockBufferBuilders);
    }

    public boolean buffersUploaded() {
        return uploadsFuture.isDone();
    }

    @Override
    public VertexBuffer getBuffer(RenderLayer layer) {
        return vertexBuffers.get(layer);
    }

    @Override
    public void close() {
        this.vertexBuffers.values().forEach(VertexBuffer::close);
    }

    @Override
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
                    final var bufferBuilder = blockBufferBuilders.get(layer);
                    final var vertexBuffer = vertexBuffers.get(layer);

                    return RenderHelper.scheduleUpload(bufferBuilder, vertexBuffer);
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

                blockRenderManager.renderFluid(pos, blueprintData, bufferBuilder, blockState, fluidState);
            }

            if(blockState.getRenderType() == BlockRenderType.INVISIBLE) continue;

            final RenderLayer blockLayer = RenderLayers.getBlockLayer(blockState);
            final BufferBuilder bufferBuilder = blockBufferBuilders.get(blockLayer);
            initLayer(blockLayer, bufferBuilder);

            matrixStack.push();
            matrixStack.translate(pos.getX() & 0xf, pos.getY() & 0xf, pos.getZ() & 0xf);

            blockRenderManager.renderBlock(blockState, pos, blueprintData, matrixStack, bufferBuilder,  true, getClient().world.random);
            nonEmptyLayers.add(blockLayer);

            matrixStack.pop();
        }
        if(nonEmptyLayers.contains(RenderLayer.getTranslucent())) {
            final BufferBuilder translucentBuilder = blockBufferBuilders.get(RenderLayer.getTranslucent());
            translucentBuilder.setSorter(VertexSorter.BY_DISTANCE);
            bufferState = translucentBuilder.getSortingData();
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
