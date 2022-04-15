package org.minefortress.selections.renderer.selection;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import org.minefortress.selections.SelectionManager;

import java.util.Collections;

public class SelectionModelBuilder {

    private final BufferBuilder bufferBuilder;
    private final BlockBufferBuilderStorage blockBufferBuilderStorage;
    private final SelectionManager selectionManager;

    private BuiltSelection builtSelection;

    public SelectionModelBuilder(BufferBuilder lineBufferBuilder, BlockBufferBuilderStorage blockBufferBuilderStorage, SelectionManager selectionManager) {
        this.bufferBuilder = lineBufferBuilder;
        this.blockBufferBuilderStorage = blockBufferBuilderStorage;
        this.selectionManager = selectionManager;
    }

    public void build() {
        final SelectionRenderInfo activeSelectionInfo = new SelectionRenderInfo(
                selectionManager.getClickType(),
                selectionManager.getClickColor(),
                selectionManager.getSelectedBlocks(),
                selectionManager.getClickingBlock());

        if(this.builtSelection != null) {
            this.builtSelection.close();
        }

        this.builtSelection = new BuiltSelection(Collections.singletonList(activeSelectionInfo));
        this.builtSelection.build(bufferBuilder, blockBufferBuilderStorage);
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
