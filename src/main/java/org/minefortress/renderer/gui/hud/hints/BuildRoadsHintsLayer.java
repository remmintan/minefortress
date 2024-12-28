package org.minefortress.renderer.gui.hud.hints;

import net.remmintan.gobi.SelectionType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.gui.hud.HudState;

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
        return CoreModUtils.getManagersProvider().get_SelectionManager();
    }
}
