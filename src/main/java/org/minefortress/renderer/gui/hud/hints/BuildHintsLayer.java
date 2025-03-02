package org.minefortress.renderer.gui.hud.hints;


import net.remmintan.gobi.SelectionType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.gui.hud.HudState;

import java.util.List;
import java.util.Optional;

public class BuildHintsLayer extends AbstractHintsLayer{

    private static final List<String> START_HINTS = List.of(
            "left click - dig",
            "right click - build"
    );

    private static final List<String> REMOVE_HINTS = List.of(
            "left click - confirm task",
            "right click - cancel",
            "ctrl + E - move up",
            "ctrl + Q - move down"
    );

    private static final List<String> BUILD_HINTS = List.of(
            "left click - cancel",
            "right click - confirm task",
            "ctrl + E - move up",
            "ctrl + Q - move down"
    );

    @Override
    public boolean shouldRender(HudState hudState) {
        final var selectType = getSelectionManager().getCurrentSelectionType();
        return super.shouldRender(hudState) && hudState == HudState.BUILD &&
                selectType != SelectionType.TREE && selectType != SelectionType.ROADS;
    }

    @Override
    protected List<String> getHints() {
        if(getSelectionManager().isSelecting()) {
            if(getSelectionManager().getClickType() == ClickType.REMOVE) {
                return REMOVE_HINTS;
            } else {
                return BUILD_HINTS;
            }
        } else {
            return START_HINTS;
        }
    }

    @Override
    protected Optional<String> getInfoText() {
        final var name = getSelectionManager().getCurrentSelectionType().getDisplayName();
        return Optional.of("Selection type: " + name);
    }

    private ISelectionManager getSelectionManager() {
        return ClientModUtils.getManagersProvider().get_SelectionManager();
    }
}
