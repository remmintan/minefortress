package org.minefortress.renderer.gui.hud.hints;

import net.remmintan.gobi.SelectionType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.minefortress.renderer.gui.hud.HudState;

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

    private ISelectionManager getSelectionManager() {
        return CoreModUtils.getMineFortressManagersProvider().get_SelectionManager();
    }

}
