package net.remmintan.panama.model;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector4f;
import net.remmintan.panama.model.BuiltModel;
import org.minefortress.selections.ClientSelection;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class BuiltTasks implements BuiltModel {
    private static final Box BOX = Box.from(new Vec3d(0, 0, 0));

    private final VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
    private final Set<ClientSelection> tasks;

    private boolean initialized = false;
    private boolean notEmpty = false;

    private CompletableFuture<Void> upload;

    public BuiltTasks(Set<ClientSelection> tasks) {
        this.tasks = new HashSet<>(tasks);
    }

    public void build(BufferBuilder bufferBuilder) {
        this.render(bufferBuilder);
        this.upload(bufferBuilder);
    }

    @Override
    public boolean hasLayer(RenderLayer layer) {
        return this.upload!=null && this.upload.isDone() && layer == RenderLayer.getLines() && notEmpty;
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

        for(ClientSelection selection: tasks)
            renderBlock(bufferBuilder, matrices, selection::shouldRenderBlock, selection, selection.getColor());


        if(initialized) bufferBuilder.end();
    }

    private void renderBlock(BufferBuilder bufferBuilder, MatrixStack matrices, BiFunction<World, BlockPos, Boolean> shouldRender, ClientSelection selection, Vector4f color) {
        init(bufferBuilder);
        final Set<BlockPos> positions = selection.getBlockPositions();
        for(BlockPos pos: positions) {
            if(!shouldRender.apply(getWorld(), pos)) continue;
            matrices.push();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            WorldRenderer.drawBox(matrices, bufferBuilder, BOX, color.x(), color.y(), color.z(), color.w());
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
            this.upload = scheduleUpload(bufferBuilder.end(), buffer);
        }
    }

    public CompletableFuture<Void> scheduleUpload(BufferBuilder.BuiltBuffer builtBuffer, VertexBuffer glBuffer) {
        Runnable runnable = () -> {
            if (!glBuffer.isClosed()) {
                glBuffer.bind();
                glBuffer.upload(builtBuffer);
                VertexBuffer.unbind();
            }
        };
        return CompletableFuture.runAsync(runnable);
    }

    private ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }
}
