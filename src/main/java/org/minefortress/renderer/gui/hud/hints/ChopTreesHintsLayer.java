package org.minefortress.renderer.gui.hud.hints;

import org.minefortress.renderer.gui.hud.HudState;
import org.minefortress.selections.SelectionManager;
import org.minefortress.selections.SelectionType;
import org.minefortress.utils.ModUtils;

import java.util.List;

public class ChopTreesHintsLayer extends AbstractHintsLayer {

    private static final List<String> START_HINTS = List.of(
            "left click - start tree",
            "selection"
    );

    private static final List<String> CHOPPING_HINTS = List.of(
            "left click - confirm task",
            "right click - cancel"
    );

    @Override
    protected List<String> getHints() {
        return getSelectionManager().isSelecting() ? CHOPPING_HINTS : START_HINTS;
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        final var sm = getSelectionManager();
        return super.shouldRender(hudState) && hudState == HudState.BUILD && sm.getCurrentSelectionType() == SelectionType.TREE;
    }

    private SelectionManager getSelectionManager() {
        return ModUtils.getFortressClient().get_SelectionManager();
    }

}
