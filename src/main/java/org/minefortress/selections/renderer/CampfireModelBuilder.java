package org.minefortress.selections.renderer;

import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;

public class CampfireModelBuilder {

    private final BlockBufferBuilderStorage blockBufferBuilders;

    private BuiltCampfire builtCampfire;

    public CampfireModelBuilder(BufferBuilderStorage bufferBuilders) {
        this.blockBufferBuilders = bufferBuilders.getBlockBufferBuilders();
    }

    public void build() {
        if(this.builtCampfire == null) {
            this.builtCampfire = new BuiltCampfire();
            this.builtCampfire.build(blockBufferBuilders);
        }
    }

    public BuiltCampfire getOrBuildCampfire() {
        if(builtCampfire == null) {
            build();
        }
        return this.builtCampfire;
    }

}
