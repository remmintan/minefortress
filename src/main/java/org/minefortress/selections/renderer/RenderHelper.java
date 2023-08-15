package org.minefortress.selections.renderer;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;

import java.util.concurrent.CompletableFuture;

public class RenderHelper {

    public static CompletableFuture<Void> scheduleUpload(BufferBuilder bufferBuilder, VertexBuffer glBuffer) {
        Runnable runnable = () -> {
            if (!glBuffer.isClosed()) {
                glBuffer.bind();
                glBuffer.upload(bufferBuilder.end());
                VertexBuffer.unbind();
            }
        };
        return CompletableFuture.runAsync(runnable);
    }

}
