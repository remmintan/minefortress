package net.remmintan.panama.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.remmintan.panama.RenderHelper;
import net.remmintan.panama.view.CampfireRenderView;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BuiltCampfire implements BuiltModel {
    private final Set<RenderLayer> initializedLayers = new HashSet<>();
    private final Set<RenderLayer> nonEmptyLayers = new HashSet<>();
    private final CampfireRenderView blueprintData = new CampfireRenderView();
    private final Map<RenderLayer, BufferBuilder.BuiltBuffer> builtBuffers = new HashMap<>();
    private final Map<RenderLayer, VertexBuffer> vertexBuffers = RenderLayer
            .getBlockLayers()
            .stream()
            .collect(Collectors.toMap(Function.identity(), it -> new VertexBuffer(VertexBuffer.Usage.STATIC)));
    private CompletableFuture<Void> uploadsFuture;

    public void build(BlockBufferBuilderStorage blockBufferBuilders) {
        render(blockBufferBuilders);
        uploadBuffers();
    }

    @Override
    public boolean hasLayer(RenderLayer layer) {
        if(uploadsFuture.isCompletedExceptionally())
            throw new IllegalStateException("Render buffers uploads failed");

        return uploadsFuture.isDone() && nonEmptyLayers.contains(layer);
    }

    @Override
    public VertexBuffer getBuffer(RenderLayer layer) {
        return vertexBuffers.get(layer);
    }

    @Override
    public void close() {
        vertexBuffers.values().forEach(VertexBuffer::close);
    }


    private void render(BlockBufferBuilderStorage blockBufferBuilders) {
        final var world = getClient().world;
        if(world == null) throw new IllegalStateException("World is null");

        BlockPos pos = BlockPos.ORIGIN;
        final BlockRenderManager blockRenderManager = getClient().getBlockRenderManager();
        BlockModelRenderer.enableBrightnessCache();
        final BlockState blockState = blueprintData.getBlockState(pos);
        final RenderLayer blockLayer = RenderLayers.getBlockLayer(blockState);
        final BufferBuilder bufferBuilder = blockBufferBuilders.get(blockLayer);
        initLayer(blockLayer, bufferBuilder);
        blockRenderManager.renderBlock(blockState, pos, blueprintData, new MatrixStack(), bufferBuilder, false, world.random);
        nonEmptyLayers.add(blockLayer);

        builtBuffers.put(blockLayer, bufferBuilder.end());

        BlockModelRenderer.disableBrightnessCache();
    }

    private void uploadBuffers() {
        final var uploadFutures = initializedLayers
                .stream()
                .map(layer -> {
                    final var builtBuffer = builtBuffers.get(layer);
                    final var vertexBuffer = vertexBuffers.get(layer);

                    return RenderHelper.scheduleUpload(builtBuffer, vertexBuffer);
                })
                .toArray(CompletableFuture[]::new);

        this.uploadsFuture = CompletableFuture.allOf(uploadFutures);
    }

    private void initLayer(RenderLayer renderLayer, BufferBuilder bufferBuilder) {
        if (initializedLayers.add(renderLayer))
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
    }

    @NotNull
    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

}
