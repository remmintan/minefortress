package org.minefortress.renderer.gui.hud.hints;

import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import org.minefortress.renderer.gui.hud.HudState;
import org.minefortress.selections.SelectionManager;
import org.minefortress.selections.SelectionType;
import org.minefortress.utils.ModUtils;

import java.util.List;

public class BuildRoadsHintsLayer extends AbstractHintsLayer{

    private final static List<String> START_BUILDING_HINTS = List.of(
            "put any block in your hand",
            "right click - start road"
    );

    private final static List<String> BUILDING_HINTS = List.of(
            "left click - cancel",
            "right click - confirm task",
            "ctrl + E - expand road",
            "ctrl + Q - shrink road"
    );

    @Override
    protected List<String> getHints() {
        return getSelectionManager().isSelecting() ? BUILDING_HINTS : START_BUILDING_HINTS;
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        final var sm = getSelectionManager();
        return super.shouldRender(hudState) && hudState == HudState.BUILD && sm.getCurrentSelectionType() == SelectionType.ROADS;
    }

    private ISelectionManager getSelectionManager() {
        return ModUtils.getFortressClient().get_SelectionManager();
    }
}
