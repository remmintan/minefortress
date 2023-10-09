package net.remmintan.panama.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.remmintan.panama.model.BuiltModel;
import net.remmintan.panama.model.builder.SelectionModelBuilder;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class SelectionRenderer extends AbstractCustomRenderer {

    private static final Vector3f WRONG_PLACEMENT_COLOR = new Vector3f(1.0F, 0.5F, 0.5F);
    private static final Vector3f CORRECT_PLACEMENT_COLOR = new Vector3f(1F, 1.0F, 1F);

    private final Supplier<ISelectionInfoProvider> selectionInfoProviderSupplier;
    private final SelectionModelBuilder selectionModelBuilder;

    public SelectionRenderer(
            MinecraftClient client,
            Map<RenderLayer, BufferBuilder> selectionBufferBuilderStorage,
            BlockBufferBuilderStorage blockBufferBuilderStorage,
            Supplier<ISelectionInfoProvider> selectionInfoProviderSupplier,
            Supplier<ISelectionModelBuilderInfoProvider> infoProviderSupplier
    ) {
        super(client);
        this.selectionInfoProviderSupplier = selectionInfoProviderSupplier;
        this.selectionModelBuilder = new SelectionModelBuilder(selectionBufferBuilderStorage, blockBufferBuilderStorage, infoProviderSupplier);
    }

    @Override
    protected Optional<BuiltModel> getBuiltModel() {
        return Optional.ofNullable(selectionModelBuilder.getBuiltSelection());
    }

    @Override
    protected boolean shouldRender() {
        return !client.options.hudHidden && getSelectionInfoProvider().isSelecting();
    }

    @Override
    public void prepareForRender() {
        if(shouldRender() && getSelectionInfoProvider().isNeedsUpdate()) {
            selectionModelBuilder.build();
            getSelectionInfoProvider().setNeedsUpdate(false);
        }
    }

    @Override
    public void close() {
        selectionModelBuilder.close();
    }

    @Override
    protected List<RenderLayer> getRenderLayers() {
        return Arrays.asList(
                RenderLayer.getLines(),
                RenderLayer.getSolid(),
                RenderLayer.getCutoutMipped(),
                RenderLayer.getCutout(),
                FortressRenderLayer.getLinesNoDepth()
        );
    }

    @Override
    protected Vector3f getColorModulator() {
        return getSelectionInfoProvider().isInCorrectState() ? CORRECT_PLACEMENT_COLOR : WRONG_PLACEMENT_COLOR;
    }

    private ISelectionInfoProvider getSelectionInfoProvider() {
        return selectionInfoProviderSupplier.get();
    }
}
