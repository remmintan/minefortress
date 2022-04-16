package org.minefortress.selections.renderer.selection;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import org.minefortress.selections.SelectionManager;

import java.util.Map;

public class SelectionModelBuilder {

    private final Map<RenderLayer, BufferBuilder> lineBufferBuilderStorage;
    private final BlockBufferBuilderStorage blockBufferBuilderStorage;
    private final SelectionManager selectionManager;

    private BuiltSelection builtSelection;

    public SelectionModelBuilder(Map<RenderLayer, BufferBuilder> lineBufferBuilderStorage, BlockBufferBuilderStorage blockBufferBuilderStorage, SelectionManager selectionManager) {
        this.lineBufferBuilderStorage = lineBufferBuilderStorage;
        this.blockBufferBuilderStorage = blockBufferBuilderStorage;
        this.selectionManager = selectionManager;
    }

    public void build() {
        final SelectionRenderInfo activeSelectionInfo = new SelectionRenderInfo(
                selectionManager.getClickType(),
                selectionManager.getClickColor(),
                selectionManager.getSelectedBlocks(),
                selectionManager.getClickingBlock(),
                selectionManager.getSelectionDimensions());

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
