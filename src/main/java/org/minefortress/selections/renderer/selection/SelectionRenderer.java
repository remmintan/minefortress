package org.minefortress.selections.renderer.selection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.custom.AbstractCustomRenderer;
import org.minefortress.renderer.custom.BuiltModel;
import org.minefortress.selections.SelectionManager;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SelectionRenderer extends AbstractCustomRenderer {

    private final SelectionManager selectionManager;
    private final SelectionModelBuilder selectionModelBuilder;

    public SelectionRenderer(MinecraftClient client, BufferBuilder selectionBufferBuilder) {
        super(client);
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
        this.selectionManager = fortressClient.getSelectionManager();
        this.selectionModelBuilder = new SelectionModelBuilder(selectionBufferBuilder, selectionManager);
    }

    @Override
    protected Optional<BlockPos> getRenderTargetPosition() {
        if(shouldRender()) {
            return Optional.of(BlockPos.ORIGIN);
        }
        return Optional.empty();
    }

    @Override
    protected Optional<BuiltModel> getBuiltModel() {
        return Optional.ofNullable(selectionModelBuilder.getBuiltSelection());
    }

    @Override
    protected boolean shouldRender() {
        return selectionManager.isSelecting();
    }

    @Override
    public void prepareForRender() {
        if(shouldRender())
            selectionModelBuilder.build();
    }

    @Override
    protected List<RenderLayer> getRenderLayers() {
        return Collections.singletonList(RenderLayer.getLines());
    }
}
