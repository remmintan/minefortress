package org.minefortress;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.OptionalDouble;

public abstract class FortressRenderLayer extends RenderLayer {

    public static final MultiPhase LINES_NO_DEPTH = RenderLayer.of("lines", VertexFormats.LINES, VertexFormat.DrawMode.LINES, 256, MultiPhaseParameters.builder().shader(LINES_SHADER).lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty())).layering(VIEW_OFFSET_Z_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_TARGET).writeMaskState(ALL_MASK).cull(DISABLE_CULLING).depthTest(ALWAYS_DEPTH_TEST).build(false));

    public FortressRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static MultiPhase getLinesNoDepth() {
        return LINES_NO_DEPTH;
    }
}
