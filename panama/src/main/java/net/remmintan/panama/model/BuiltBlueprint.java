package net.remmintan.panama.model;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.remmintan.panama.RenderHelper;
import net.remmintan.panama.view.BlueprintBlockRenderView;
import org.minefortress.blueprints.data.BlueprintDataLayer;
import org.minefortress.blueprints.data.StrctureBlockData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BuiltBlueprint implements BuiltModel {

    private final Set<RenderLayer> initializedLayers = new HashSet<>();
    private final Set<RenderLayer> nonEmptyLayers = new HashSet<>();
    private final Map<RenderLayer, VertexBuffer> vertexBuffers = RenderLayer
            .getBlockLayers()
            .stream()
            .collect(Collectors.toMap(Function.identity(), it -> new VertexBuffer(VertexBuffer.Usage.STATIC)));
    private final Map<RenderLayer, BufferBuilder.BuiltBuffer> builtBuffers = new HashMap<>();


    private CompletableFuture<Void> uploadsFuture;
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
        uploadBuffers();
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

    private void uploadBuffers() {
        final var uploadFutures = initializedLayers
                .stream()
                .map(layer -> {
                    final var bufferBuilder = builtBuffers.get(layer);
                    final var vertexBuffer = vertexBuffers.get(layer);

                    return RenderHelper.scheduleUpload(bufferBuilder, vertexBuffer);
                })
                .toArray(CompletableFuture[]::new);

        uploadsFuture = CompletableFuture.allOf(uploadFutures);
    }


    private void render(BlockBufferBuilderStorage blockBufferBuilders) {
        BlockPos minPos = BlockPos.ORIGIN;
        BlockPos maxPos = minPos.add(15, 15,15);

        MatrixStack matrixStack = new MatrixStack();

        BlockModelRenderer.enableBrightnessCache();
        final BlockRenderManager blockRenderManager = getClient().getBlockRenderManager();

        for(BlockPos pos : BlockPos.iterate(minPos, maxPos)) {
            final BlockState blockState = blueprintData.getBlockState(pos);
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
            matrixStack.translate(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);

            blockRenderManager.renderBlock(blockState, pos, blueprintData, matrixStack, bufferBuilder,  true, getClient().world.random);
            nonEmptyLayers.add(blockLayer);

            matrixStack.pop();
        }

        for (RenderLayer layer : initializedLayers) {
            builtBuffers.put(layer, blockBufferBuilders.get(layer).end());
        }

        BlockModelRenderer.disableBrightnessCache();
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
