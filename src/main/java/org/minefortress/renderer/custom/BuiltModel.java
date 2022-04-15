package org.minefortress.renderer.custom;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;

public interface BuiltModel {

    boolean hasLayer(RenderLayer layer);
    VertexBuffer getBuffer(RenderLayer layer);
    void close();

}
