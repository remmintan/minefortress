package org.minefortress.selections.renderer.selection;

import com.mojang.datafixers.util.Pair;
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
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.minefortress.renderer.FortressRenderLayer;
import org.minefortress.renderer.custom.BuiltModel;
import org.minefortress.selections.ClickType;
import org.minefortress.tasks.BuildingManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BuiltSelection implements BuiltModel {

    private static final Box BOX = Box.from(new Vec3d(0, 0, 0));

    private final SelectionBlockRenderView selectionBlockRenderView;

    private final Set<RenderLayer> initializedLayers = new HashSet<>();
    private final Set<RenderLayer> nonEmptyLayers = new HashSet<>();
    private final Map<RenderLayer, VertexBuffer> buffers = new HashMap<>();
    private CompletableFuture<List<Void>> upload;

    private final SelectionRenderInfo selection;

    public BuiltSelection(SelectionRenderInfo selection) {
        this.selectionBlockRenderView = new SelectionBlockRenderView(
                (p, c) -> getWorld().getColor(getBlockPos(), c)
        );
        this.selection = selection;

        for(RenderLayer layer : RenderLayer.getBlockLayers()) {
            buffers.put(layer, new VertexBuffer());
        }
        buffers.put(RenderLayer.getLines(), new VertexBuffer());
        buffers.put(FortressRenderLayer.getLinesNoDepth(), new VertexBuffer());
    }

    private BlockPos getBlockPos() {
        return getClient().player!=null? getClient().player.getBlockPos():getWorld().getSpawnPos();
    }

    public void build(Map<RenderLayer, BufferBuilder> lineBufferBuilderStorage, BlockBufferBuilderStorage blockBufferBuilderStorage) {
        render(lineBufferBuilderStorage, blockBufferBuilderStorage);
        uploadBuffers(lineBufferBuilderStorage, blockBufferBuilderStorage);
    }

    private void render(Map<RenderLayer, BufferBuilder> lineBufferBuilderStorage, BlockBufferBuilderStorage blockBufferBuilderStorage) {
        final MatrixStack matrices = new MatrixStack();

        final RenderLayer lines = RenderLayer.getLines();
        final BufferBuilder linesBufferBuilder = lineBufferBuilderStorage.get(lines);
           init(lines, linesBufferBuilder);
        final ClickType clickType = selection.getClickType();
        final Vector4f color = selection.getColor();
        final List<BlockPos> positions = selection.getPositions()
                .stream()
                .filter(getShouldRenderPosPredicate(clickType))
                .collect(Collectors.toList());
        final BlockState blockState = selection.getBlockState();
        selectionBlockRenderView.setBlockStateSupplier((blockPos) -> positions.contains(blockPos)?blockState: Blocks.AIR.getDefaultState());
        for (BlockPos pos : positions) {

            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            WorldRenderer.drawBox(matrices, linesBufferBuilder, BOX, color.getX(), color.getY(), color.getZ(), color.getW());
            if(clickType == ClickType.BUILD || clickType == ClickType.ROADS) {
                renderFluid(blockBufferBuilderStorage, pos, blockState);
                renderBlock(blockBufferBuilderStorage, matrices, pos, blockState);
            }
            matrices.pop();

            nonEmptyLayers.add(RenderLayer.getLines());
        }

        if(clickType == ClickType.REMOVE) {
            final RenderLayer linesNoDepth = FortressRenderLayer.getLinesNoDepth();
            final BufferBuilder linesNoDepthBufferBuilder = lineBufferBuilderStorage.get(linesNoDepth);
            init(linesNoDepth, linesNoDepthBufferBuilder);

            final List<Pair<Vec3i, Vec3i>> selectionDimensions = selection.getSelectionDimensions();
            for (Pair<Vec3i, Vec3i> dimension : selectionDimensions) {
                final Vec3i size = dimension.getFirst();
                final Vec3i start = dimension.getSecond();

                final Box sizeBox = new Box(0, 0, 0, size.getX(), size.getY(), size.getZ());
                matrices.push();
                matrices.translate(start.getX(), start.getY(), start.getZ());
                WorldRenderer.drawBox(matrices, linesNoDepthBufferBuilder, sizeBox, color.getX(), color.getY(), color.getZ(), color.getW());
                matrices.pop();

                nonEmptyLayers.add(FortressRenderLayer.getLinesNoDepth());
            }
        }

        for(RenderLayer initializedLayer : this.initializedLayers) {
            if(initializedLayer == RenderLayer.getLines() || initializedLayer == FortressRenderLayer.getLinesNoDepth())
                lineBufferBuilderStorage.get(initializedLayer).end();
            else
                blockBufferBuilderStorage.get(initializedLayer).end();
        }
    }

    @NotNull
    private Predicate<BlockPos> getShouldRenderPosPredicate(ClickType clickType) {
        return pos ->
                (clickType == ClickType.BUILD && BuildingManager.canPlaceBlock(getWorld(),pos)) ||
                ((clickType == ClickType.REMOVE || clickType == ClickType.ROADS) && BuildingManager.canRemoveBlock(getWorld(),pos));
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
            if(layer == RenderLayer.LINES || layer == FortressRenderLayer.LINES_NO_DEPTH) {
                bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
            } else {
                bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            }
        }
    }

    private void uploadBuffers(Map<RenderLayer, BufferBuilder> lineBufferBuilderStorage, BlockBufferBuilderStorage blockBufferBuilderStorage) {
        final List<CompletableFuture<Void>> uploads = initializedLayers
                .stream()
                .map(layer -> {
                    final VertexBuffer vertexBuffer = buffers.get(layer);
                    if (layer == RenderLayer.getLines() || layer == FortressRenderLayer.getLinesNoDepth()) {
                        final BufferBuilder bufferBuilder = lineBufferBuilderStorage.get(layer);
                        return vertexBuffer
                                .submitUpload(bufferBuilder)
                                .whenComplete((r, t) -> {
                                    if (t != null) {
                                        CrashReport crashReport = CrashReport.create(t, "Building selection lines");
                                        getClient()
                                                .setCrashReportSupplier(() -> getClient().addDetailsToCrashReport(crashReport));
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
                                        getClient().setCrashReportSupplier(() -> getClient().addDetailsToCrashReport(crashReport));
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
