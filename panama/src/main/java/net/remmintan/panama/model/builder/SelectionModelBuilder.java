package net.remmintan.panama.model.builder;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.remmintan.gobi.renderer.selection.SelectionRenderInfo;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionModelBuilderInfoProvider;
import net.remmintan.panama.model.BuiltSelection;

import java.util.Map;
import java.util.function.Supplier;

public class SelectionModelBuilder {

    private final Map<RenderLayer, BufferBuilder> lineBufferBuilderStorage;
    private final BlockBufferBuilderStorage blockBufferBuilderStorage;
    private final Supplier<ISelectionModelBuilderInfoProvider> infoProviderSupplier;

    private BuiltSelection builtSelection;

    public SelectionModelBuilder(
            Map<RenderLayer, BufferBuilder> lineBufferBuilderStorage,
            BlockBufferBuilderStorage blockBufferBuilderStorage,
            Supplier<ISelectionModelBuilderInfoProvider> infoProviderSupplier
    ) {
        this.lineBufferBuilderStorage = lineBufferBuilderStorage;
        this.blockBufferBuilderStorage = blockBufferBuilderStorage;
        this.infoProviderSupplier = infoProviderSupplier;
    }

    public void build() {
        final var infoProvider = infoProviderSupplier.get();
        final SelectionRenderInfo activeSelectionInfo = new SelectionRenderInfo(
                infoProvider.getClickType(),
                infoProvider.getClickColor(),
                infoProvider.getSelectedBlocks(),
                infoProvider.getClickingBlock(),
                infoProvider.getSelectionDimensions()
        );

        if(this.builtSelection != null) {
            this.builtSelection.close();
        }

        this.builtSelection = new BuiltSelection(activeSelectionInfo);
        this.builtSelection.build(lineBufferBuilderStorage, blockBufferBuilderStorage);
    }

    public BuiltSelection getBuiltSelection() {
        return this.builtSelection;
    }

    public void close() {
        if(this.builtSelection != null) {
            this.builtSelection.close();
        }
    }

}
