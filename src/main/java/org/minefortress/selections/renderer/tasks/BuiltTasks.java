package org.minefortress.selections.renderer.tasks;

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
import org.minefortress.selections.ClientSelection;
import org.minefortress.tasks.BuildingManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class BuiltTasks implements BuiltModel {

    private static final Vector4f DESTROY_COLOR = new Vector4f(170f/255f, 0, 0, 1f);
    private static final Vector4f BUILD_COLOR = new Vector4f(0, 170f/255f, 0, 1f);
    private static final Box BOX = Box.from(new Vec3d(0, 0, 0));

    private final VertexBuffer buffer = new VertexBuffer();
    private final Set<ClientSelection> buildTasks;
    private final Set<ClientSelection> removeTasks;

    private boolean initialized = false;
    private boolean notEmpty = false;

    private CompletableFuture<Void> upload;

    public BuiltTasks(Set<ClientSelection> buildTasks, Set<ClientSelection> removeTasks) {
        this.buildTasks = new HashSet<>(buildTasks);
        this.removeTasks = new HashSet<>(removeTasks);
    }

    public void build(BufferBuilder bufferBuilder) {
        this.render(bufferBuilder);
        this.upload(bufferBuilder);
    }

    @Override
    public boolean hasLayer(RenderLayer layer) {
        return this.upload.isDone() && layer == RenderLayer.getLines() && notEmpty;
    }

    @Override
    public VertexBuffer getBuffer(RenderLayer layer) {
        if(layer != RenderLayer.getLines()) throw new IllegalArgumentException("Only lines are supported");
        return buffer;
    }

    @Override
    public void close() {
        buffer.close();
    }

    private void render(BufferBuilder bufferBuilder) {
        final MatrixStack matrices = new MatrixStack();

        final Function<BlockPos, Boolean> canBuild = pos -> BuildingManager.canPlaceBlock(getWorld(), pos);
        final Function<BlockPos, Boolean> canRemove = pos -> BuildingManager.canRemoveBlock(getWorld(), pos);

        for(ClientSelection selection: buildTasks)
            renderBlock(bufferBuilder, matrices, canBuild, selection, BUILD_COLOR);

        for(ClientSelection selection: removeTasks)
            renderBlock(bufferBuilder, matrices, canRemove, selection, DESTROY_COLOR);


        if(initialized) bufferBuilder.end();
    }

    private void renderBlock(BufferBuilder bufferBuilder, MatrixStack matrices, Function<BlockPos, Boolean> shouldRender, ClientSelection selection, Vector4f color) {
        init(bufferBuilder);
        final Set<BlockPos> positions = selection.getBlockPositions();
        for(BlockPos pos: positions) {
            if(!shouldRender.apply(pos)) continue;
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            WorldRenderer.drawBox(matrices, bufferBuilder, BOX, color.getX(), color.getY(), color.getZ(), color.getW());
            matrices.pop();
            notEmpty = true;
        }
    }

    private void init(BufferBuilder bufferBuilder) {
        if(!initialized) {
            this.initialized = true;
            bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        }
    }

    private void upload(BufferBuilder bufferBuilder){
        if(initialized) {
            this.upload = buffer.submitUpload(bufferBuilder)
                    .whenComplete((aVoid, throwable) -> {
                        if (throwable != null) {
                            CrashReport crashReport = CrashReport.create(throwable, "Building tasks model");
                            MinecraftClient.getInstance().setCrashReport(MinecraftClient.getInstance().addDetailsToCrashReport(crashReport));
                            return;
                        }

                        bufferBuilder.clear();
                    });
        }
    }

    private ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }
}
