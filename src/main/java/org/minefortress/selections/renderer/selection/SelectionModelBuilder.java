package org.minefortress.selections.renderer.selection;

import net.minecraft.client.render.BufferBuilder;
import org.minefortress.selections.SelectionManager;

import java.util.Collections;

public class SelectionModelBuilder {

    private final BufferBuilder bufferBuilder;
    private final SelectionManager selectionManager;

    private BuiltSelection builtSelection;

    public SelectionModelBuilder(BufferBuilder bufferBuilder, SelectionManager selectionManager) {
        this.bufferBuilder = bufferBuilder;
        this.selectionManager = selectionManager;
    }

    public void build() {
        final SelectionRenderInfo activeSelectionInfo = new SelectionRenderInfo(
                selectionManager.getClickType(),
                selectionManager.getClickColor(),
                selectionManager.getSelectedBlocks());

        this.builtSelection = new BuiltSelection(Collections.singletonList(activeSelectionInfo));
        this.builtSelection.build(bufferBuilder);
    }

    public BuiltSelection getBuiltSelection() {
        return this.builtSelection;
    }

}
