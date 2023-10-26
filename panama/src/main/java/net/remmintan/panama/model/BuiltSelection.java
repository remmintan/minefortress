package net.remmintan.panama.model;

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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.remmintan.gobi.renderer.selection.SelectionRenderInfo;
import net.remmintan.mods.minefortress.building.BuildingHelper;
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType;
import net.remmintan.panama.RenderHelper;
import net.remmintan.panama.renderer.FortressRenderLayer;
import net.remmintan.panama.view.SelectionBlockRenderView;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class BuiltSelection implements BuiltModel {

    private static final Box BOX = Box.from(new Vec3d(0, 0, 0));

    private final SelectionBlockRenderView selectionBlockRenderView;

    private final Set<RenderLayer> initializedLayers = new HashSet<>();
    private final Set<RenderLayer> nonEmptyLayers = new HashSet<>();
    private final Map<RenderLayer, VertexBuffer> vertexBuffers = new HashMap<>();
    private final Map<RenderLayer, BufferBuilder.BuiltBuffer> builtBufferMap = new HashMap<>();
    private CompletableFuture<Void> upload;

    private final SelectionRenderInfo selection;

    public BuiltSelection(SelectionRenderInfo selection) {
        this.selectionBlockRenderView = new SelectionBlockRenderView(
                (p, c) -> getWorld().getColor(getBlockPos(), c)
        );
        this.selection = selection;

        for(RenderLayer layer : RenderLayer.getBlockLayers()) {
            vertexBuffers.put(layer, new VertexBuffer(VertexBuffer.Usage.STATIC));
        }
        vertexBuffers.put(RenderLayer.getLines(), new VertexBuffer(VertexBuffer.Usage.STATIC));
        vertexBuffers.put(FortressRenderLayer.getLinesNoDepth(), new VertexBuffer(VertexBuffer.Usage.STATIC));
    }

    private BlockPos getBlockPos() {
        return Optional.ofNullable(getClient().player).map(PlayerEntity::getBlockPos).orElse(getWorld().getSpawnPos());
    }

    public void build(Map<RenderLayer, BufferBuilder> lineBufferBuilderStorage, BlockBufferBuilderStorage blockBufferBuilderStorage) {
        render(lineBufferBuilderStorage, blockBufferBuilderStorage);
        uploadBuffers();
    }

    private void render(Map<RenderLayer, BufferBuilder> lineBufferBuilderStorage, BlockBufferBuilderStorage blockBufferBuilderStorage) {
        final MatrixStack matrices = new MatrixStack();

        final RenderLayer lines = RenderLayer.getLines();
        final BufferBuilder linesBufferBuilder = lineBufferBuilderStorage.get(lines);
        init(lines, linesBufferBuilder);
        final ClickType clickType = selection.clickType();
        final Vector4f color = selection.color();
        final List<BlockPos> positions = selection.positions()
                .stream()
                .filter(getShouldRenderPosPredicate(clickType))
                .toList();
        final BlockState blockState = selection.blockState();
        selectionBlockRenderView.setBlockStateSupplier((blockPos) -> positions.contains(blockPos)?blockState: Blocks.AIR.getDefaultState());
        for (BlockPos pos : positions) {
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            WorldRenderer.drawBox(matrices, linesBufferBuilder, BOX, color.x(), color.y(), color.z(), color.w());
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

            final List<Pair<Vec3i, Vec3i>> selectionDimensions = selection.selectionDimensions();
            for (Pair<Vec3i, Vec3i> dimension : selectionDimensions) {
                final Vec3i size = dimension.getFirst();
                final Vec3i start = dimension.getSecond();

                final Box sizeBox = new Box(0, 0, 0, size.getX(), size.getY(), size.getZ());
                matrices.push();
                matrices.translate(start.getX(), start.getY(), start.getZ());
                WorldRenderer.drawBox(matrices, linesNoDepthBufferBuilder, sizeBox, color.x(), color.y(), color.z(), color.w());
                matrices.pop();

                nonEmptyLayers.add(FortressRenderLayer.getLinesNoDepth());
            }
        }

        for(var layer : this.initializedLayers) {
            final BufferBuilder.BuiltBuffer builtBuffer;
            if(layer == RenderLayer.getLines() || layer == FortressRenderLayer.getLinesNoDepth()) {
                builtBuffer = lineBufferBuilderStorage.get(layer).end();
            } else {
                builtBuffer = blockBufferBuilderStorage.get(layer).end();
            }
            this.builtBufferMap.put(layer, builtBuffer);
        }
    }

    @NotNull
    private Predicate<BlockPos> getShouldRenderPosPredicate(ClickType clickType) {
        return pos ->
                (clickType == ClickType.BUILD && BuildingHelper.canPlaceBlock(getWorld(),pos)) ||
                ((clickType == ClickType.REMOVE || clickType == ClickType.ROADS) && BuildingHelper.canRemoveBlock(getWorld(),pos));
    }

    private void renderFluid(BlockBufferBuilderStorage blockBufferBuilderStorage, BlockPos pos, BlockState blockState) {
        final FluidState fluidState = blockState.getFluidState();
        if(!fluidState.isEmpty()) {
            final RenderLayer fluidRenderLayer = RenderLayers.getFluidLayer(fluidState);
            final BufferBuilder fluidBufferBuilder = blockBufferBuilderStorage.get(fluidRenderLayer);
            init(fluidRenderLayer, fluidBufferBuilder);

            getBlockRenderManager().renderFluid(pos, selectionBlockRenderView, fluidBufferBuilder, blockState, fluidState);

        }
    }

    private void renderBlock(BlockBufferBuilderStorage blockBufferBuilderStorage, MatrixStack matrices, BlockPos pos, BlockState blockState) {
        if(blockState.getRenderType() != BlockRenderType.INVISIBLE) {
            final RenderLayer blockLayer = RenderLayers.getBlockLayer(blockState);
            final BufferBuilder blockBufferBuilder = blockBufferBuilderStorage.get(blockLayer);
            init(blockLayer, blockBufferBuilder);

            final BlockRenderManager blockRenderer = getBlockRenderManager();
            blockRenderer.renderBlock(blockState, pos, selectionBlockRenderView, matrices, blockBufferBuilder, true, getWorld().random);

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

    private void uploadBuffers() {
        final var uploads = initializedLayers
                .stream()
                .map(layer -> {
                    final var vertexBuffer = vertexBuffers.get(layer);
                    final var builtBuffer = builtBufferMap.get(layer);
                    return RenderHelper.scheduleUpload(builtBuffer, vertexBuffer);
                }).toArray(CompletableFuture[]::new);
        this.upload = CompletableFuture.allOf(uploads);
    }

    @Override
    public boolean hasLayer(RenderLayer layer) {
        if(upload.isCompletedExceptionally())
            throw new IllegalStateException("Render buffers uploads failed");

        return upload.isDone() && nonEmptyLayers.contains(layer);
    }

    @Override
    public VertexBuffer getBuffer(RenderLayer layer) {
        return vertexBuffers.get(layer);
    }

    @Override
    public void close() {
        vertexBuffers.values().forEach(VertexBuffer::close);
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
