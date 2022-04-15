package org.minefortress.selections.renderer.campfire;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import org.minefortress.renderer.custom.BuiltModel;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BuiltCampfire implements BuiltModel {

    private final Random random = new Random();

    private final Set<RenderLayer> initializedLayers = new HashSet<>();
    private final Set<RenderLayer> nonEmptyLayers = new HashSet<>();

    private final CampfireRenderView blueprintData = new CampfireRenderView();
    private CompletableFuture<List<Void>> uploadsFuture;

    private final Map<RenderLayer, VertexBuffer> vertexBuffers = RenderLayer
            .getBlockLayers()
            .stream()
            .collect(Collectors.toMap(Function.identity(), it -> new VertexBuffer()));

    public void build(BlockBufferBuilderStorage blockBufferBuilders) {
        render(blockBufferBuilders);
        uploadBuffers(blockBufferBuilders);
    }

    private boolean buffersUploaded() {
        return uploadsFuture.isDone();
    }

    @Override
    public boolean hasLayer(RenderLayer layer) {
        return this.buffersUploaded() && nonEmptyLayers.contains(layer);
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
        BlockPos pos = BlockPos.ORIGIN;


        final BlockRenderManager blockRenderManager = getClient().getBlockRenderManager();

        BlockModelRenderer.enableBrightnessCache();

        final BlockState blockState = blueprintData.getBlockState(pos);

        final RenderLayer blockLayer = RenderLayers.getBlockLayer(blockState);
        final BufferBuilder bufferBuilder = blockBufferBuilders.get(blockLayer);
        initLayer(blockLayer, bufferBuilder);

        if(blockRenderManager.renderBlock(blockState, pos, blueprintData, new MatrixStack(), bufferBuilder,  true, random))
            nonEmptyLayers.add(blockLayer);

        initializedLayers.stream().map(blockBufferBuilders::get).forEach(BufferBuilder::end);
        BlockModelRenderer.disableBrightnessCache();
    }

    private void uploadBuffers(BlockBufferBuilderStorage blockBufferBuilders) {
        final List<CompletableFuture<Void>> uploadFutures = initializedLayers
                .stream()
                .map(layer -> {
                    final BufferBuilder bufferBuilder = blockBufferBuilders.get(layer);
                    VertexBuffer vertexBuffer = vertexBuffers.get(layer);

                    return vertexBuffer
                            .submitUpload(bufferBuilder)
                            .whenComplete((r, t) -> {
                               if(t != null) {
                                   CrashReport crashReport = CrashReport.create(t, "Building campfire");
                                   MinecraftClient.getInstance().setCrashReport(MinecraftClient.getInstance().addDetailsToCrashReport(crashReport));
                                   return;
                               }

                               bufferBuilder.clear();
                            });
                })
                .collect(Collectors.toList());

        uploadsFuture = Util.combine(uploadFutures);
    }

    private void initLayer(RenderLayer renderLayer, BufferBuilder bufferBuilder) {
        if(initializedLayers.add(renderLayer))
            beginBufferBuilding(bufferBuilder);
    }

    private void beginBufferBuilding(BufferBuilder buffer) {
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
    }

    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

}
