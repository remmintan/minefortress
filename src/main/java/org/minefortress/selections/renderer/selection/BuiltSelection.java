package org.minefortress.selections.renderer.selection;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;
import org.minefortress.renderer.custom.BuiltModel;
import org.minefortress.selections.ClickType;
import org.minefortress.tasks.BuildingManager;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BuiltSelection implements BuiltModel {

    private static final Box BOX = Box.from(new Vec3d(0, 0, 0));

    private boolean initialized = false;
    private boolean notEmpty = false;

    private final VertexBuffer vertexBuffer = new VertexBuffer();
    private CompletableFuture<Void> upload;

    private final List<SelectionRenderInfo> selections;

    public BuiltSelection(List<SelectionRenderInfo> selections) {
        this.selections = selections;
    }

    public void build(BufferBuilder bufferBuilder) {
        render(bufferBuilder);
        uploadBuffers(bufferBuilder);
    }

    private void render(BufferBuilder bufferBuilder) {
        final MatrixStack matrices = new MatrixStack();

        for(SelectionRenderInfo selection : selections) {
            init(bufferBuilder);
            final ClickType clickType = selection.getClickType();
            final Vector4f color = selection.getColor();
            final Set<BlockPos> positions = selection.getPositions();
            for (BlockPos pos : positions) {
                if(clickType == ClickType.BUILD && !BuildingManager.canPlaceBlock(getWorld(),pos)) continue;
                if(clickType == ClickType.REMOVE && !BuildingManager.canRemoveBlock(getWorld(),pos)) continue;

                matrices.push();
                matrices.translate(pos.getX(), pos.getY(), pos.getZ());
                WorldRenderer.drawBox(matrices, bufferBuilder, BOX, color.getX(), color.getY(), color.getZ(), color.getW());
                matrices.pop();

                notEmpty = true;
            }
        }

        bufferBuilder.end();
    }

    private void init(BufferBuilder bufferBuilder) {
        if(initialized) return;
        initialized = true;
        bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
    }

    private void uploadBuffers(BufferBuilder bufferBuilder) {
        if(initialized) {
            upload = vertexBuffer
                    .submitUpload(bufferBuilder)
                    .whenComplete((r, t) -> {
                        if(t != null) {
                            CrashReport crashReport = CrashReport.create(t, "Building campfire");
                            MinecraftClient.getInstance().setCrashReport(MinecraftClient.getInstance().addDetailsToCrashReport(crashReport));
                            return;
                        }

                        bufferBuilder.clear();
                    });
        }
    }

    @Override
    public boolean hasLayer(RenderLayer layer) {
        return layer == RenderLayer.LINES && notEmpty && upload.isDone();
    }

    @Override
    public VertexBuffer getBuffer(RenderLayer layer) {
        if(layer == RenderLayer.LINES) {
            return vertexBuffer;
        } else {
            throw new IllegalArgumentException("Invalid layer");
        }
    }

    private ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }
}
