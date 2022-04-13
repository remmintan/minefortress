package org.minefortress.selections.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import org.minefortress.renderer.custom.AbstractCustomRenderer;
import org.minefortress.renderer.custom.BuiltModel;
import org.minefortress.selections.SelectionManager;

import java.util.Optional;

public final class CampfireRenderer extends AbstractCustomRenderer {

    private final CampfireModelBuilder campfireModelBuilder;

    public CampfireRenderer(MinecraftClient client) {
        super(client);
        this.campfireModelBuilder = new CampfireModelBuilder(client.getBufferBuilders());
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

    public void prepareCampfireForRender() {
        this.campfireModelBuilder.build();
    }

}
