package org.minefortress.selections.renderer.campfire;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.util.math.BlockPos;
import org.minefortress.renderer.custom.AbstractCustomRenderer;
import org.minefortress.renderer.custom.BuiltModel;

import java.util.Optional;

public final class CampfireRenderer extends AbstractCustomRenderer {

    private final CampfireModelBuilder campfireModelBuilder;

    public CampfireRenderer(MinecraftClient client, BlockBufferBuilderStorage blockBufferBuilder) {
        super(client);
        this.campfireModelBuilder = new CampfireModelBuilder(blockBufferBuilder);
    }

    @Override
    protected Optional<BlockPos> getRenderTargetPosition() {
        if(!shouldRender())
            return Optional.empty();
        else
            return Optional.ofNullable(getClientManager().getPosAppropriateForCenter());
    }

    @Override
    protected Optional<BuiltModel> getBuiltModel() {
        return Optional.ofNullable(this.campfireModelBuilder.getOrBuildCampfire());
    }

    @Override
    protected boolean shouldRender() {
        return getClientManager().isFortressInitializationNeeded();
    }

    @Override
    public void prepareForRender() {
        this.campfireModelBuilder.build();
    }

}
