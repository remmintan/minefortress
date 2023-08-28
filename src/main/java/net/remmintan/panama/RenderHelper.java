package net.remmintan.panama;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;

import java.util.concurrent.CompletableFuture;

public class RenderHelper {

    public static CompletableFuture<Void> scheduleUpload(
            BufferBuilder.BuiltBuffer builtBuffer,
            VertexBuffer vertexBuffer
    ) {
        Runnable runnable = () -> {
            if (!vertexBuffer.isClosed()) {
                vertexBuffer.bind();
                vertexBuffer.upload(builtBuffer);
                VertexBuffer.unbind();
            }
        };
        return CompletableFuture.runAsync(runnable, MinecraftClient.getInstance());
    }



}
