package org.minefortress.selections.renderer.selection;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;
import org.minefortress.renderer.custom.BuiltModel;
import org.minefortress.selections.ClickType;
import org.minefortress.tasks.BuildingManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BuiltSelection implements BuiltModel {

    private static final Box BOX = Box.from(new Vec3d(0, 0, 0));

    private final SelectionBlockRenderView selectionBlockRenderView = new SelectionBlockRenderView();

    private final Set<RenderLayer> initializedLayers = new HashSet<>();
    private final Set<RenderLayer> nonEmptyLayers = new HashSet<>();
    private final Map<RenderLayer, VertexBuffer> buffers = new HashMap<>();
    private CompletableFuture<List<Void>> upload;

    private final List<SelectionRenderInfo> selections;

    public BuiltSelection(List<SelectionRenderInfo> selections) {
        this.selections = selections;

        for(RenderLayer layer : RenderLayer.getBlockLayers()) {
            buffers.put(layer, new VertexBuffer());
        }
        buffers.put(RenderLayer.getLines(), new VertexBuffer());
    }

    public void build(BufferBuilder bufferBuilder, BlockBufferBuilderStorage blockBufferBuilderStorage) {
        render(bufferBuilder, blockBufferBuilderStorage);
        uploadBuffers(bufferBuilder, blockBufferBuilderStorage);
    }

    private void render(BufferBuilder bufferBuilder, BlockBufferBuilderStorage blockBufferBuilderStorage) {
        final MatrixStack matrices = new MatrixStack();

        for(SelectionRenderInfo selection : selections) {
            init(RenderLayer.LINES, bufferBuilder);
            final ClickType clickType = selection.getClickType();
            final Vector4f color = selection.getColor();
            final List<BlockPos> positions = selection.getPositions();
            final BlockState blockState = selection.getBlockState();
            selectionBlockRenderView.setBlockStateSupplier((blockPos) -> positions.contains(blockPos)?blockState: Blocks.AIR.getDefaultState());
            for (BlockPos pos : positions) {
                if(clickType == ClickType.BUILD && !BuildingManager.canPlaceBlock(getWorld(),pos)) continue;
                if((clickType == ClickType.REMOVE || clickType == ClickType.ROADS) && !BuildingManager.canRemoveBlock(getWorld(),pos)) continue;

                matrices.push();
                matrices.translate(pos.getX(), pos.getY(), pos.getZ());
                WorldRenderer.drawBox(matrices, bufferBuilder, BOX, color.getX(), color.getY(), color.getZ(), color.getW());
                if(clickType == ClickType.BUILD || clickType == ClickType.ROADS) {
                    renderFluid(blockBufferBuilderStorage, pos, blockState);
                    renderBlock(blockBufferBuilderStorage, matrices, pos, blockState);
                }
                matrices.pop();

                nonEmptyLayers.add(RenderLayer.getLines());
            }
        }

        for(RenderLayer initializedLayer : this.initializedLayers) {
            if(initializedLayer == RenderLayer.LINES)
                bufferBuilder.end();
            else
                blockBufferBuilderStorage.get(initializedLayer).end();
        }
    }

    private void renderFluid(BlockBufferBuilderStorage blockBufferBuilderStorage, BlockPos pos, BlockState blockState) {
        final FluidState fluidState = blockState.getFluidState();
        if(!fluidState.isEmpty()) {
            final RenderLayer fluidRenderLayer = RenderLayers.getFluidLayer(fluidState);
            final BufferBuilder fluidBufferBuilder = blockBufferBuilderStorage.get(fluidRenderLayer);
            init(fluidRenderLayer, fluidBufferBuilder);

            if(getBlockRenderManager().renderFluid(pos, selectionBlockRenderView, fluidBufferBuilder, fluidState))
                nonEmptyLayers.add(fluidRenderLayer);
        }
    }

    private void renderBlock(BlockBufferBuilderStorage blockBufferBuilderStorage, MatrixStack matrices, BlockPos pos, BlockState blockState) {
        if(blockState.getRenderType() != BlockRenderType.INVISIBLE) {
            final RenderLayer blockLayer = RenderLayers.getBlockLayer(blockState);
            final BufferBuilder blockBufferBuilder = blockBufferBuilderStorage.get(blockLayer);
            init(blockLayer, blockBufferBuilder);


            final BlockRenderManager blockRenderer = getBlockRenderManager();
            if(blockRenderer
                    .renderBlock(blockState, pos, selectionBlockRenderView, matrices, blockBufferBuilder, true, getWorld().random))
                nonEmptyLayers.add(blockLayer);
        }
    }

    private void init(RenderLayer layer, BufferBuilder bufferBuilder) {
        if(initializedLayers.add(layer)){
            if(layer == RenderLayer.LINES){
                bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
            } else {
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            }
        }

    }

    private void uploadBuffers(BufferBuilder bufferBuilder, BlockBufferBuilderStorage blockBufferBuilderStorage) {
        final List<CompletableFuture<Void>> uploads = initializedLayers
                .stream()
                .map(layer -> {
                    final VertexBuffer vertexBuffer = buffers.get(layer);
                    if (layer == RenderLayer.LINES) {
                        return vertexBuffer
                                .submitUpload(bufferBuilder)
                                .whenComplete((r, t) -> {
                                    if (t != null) {
                                        CrashReport crashReport = CrashReport.create(t, "Building selection lines");
                                        getClient().setCrashReport(getClient().addDetailsToCrashReport(crashReport));
                                        return;
                                    }

                                    bufferBuilder.clear();
                                });
                    } else {
                        final BufferBuilder buffer = blockBufferBuilderStorage.get(layer);
                        return vertexBuffer
                                .submitUpload(buffer)
                                .whenComplete((r, t) -> {
                                    if (t != null) {
                                        CrashReport crashReport = CrashReport.create(t, "Building selection blocks");
                                        getClient().setCrashReport(getClient().addDetailsToCrashReport(crashReport));
                                        return;
                                    }

                                    buffer.clear();
                                });
                    }
                }).collect(Collectors.toList());
        this.upload = Util.combine(uploads);
    }

    @Override
    public boolean hasLayer(RenderLayer layer) {
        return upload.isDone() && nonEmptyLayers.contains(layer);
    }

    @Override
    public VertexBuffer getBuffer(RenderLayer layer) {
        return buffers.get(layer);
    }

    @Override
    public void close() {
        buffers.values().forEach(VertexBuffer::close);
    }

    private ClientWorld getWorld() {
        return getClient().world;
    }

    private BlockRenderManager getBlockRenderManager() {
        return getClient().getBlockRenderManager();
    }

    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }
}
