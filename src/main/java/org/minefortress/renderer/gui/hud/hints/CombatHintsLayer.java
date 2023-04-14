package org.minefortress.renderer.gui.hud.hints;

import org.minefortress.renderer.gui.hud.HudState;
import org.minefortress.utils.ModUtils;

import java.util.List;

public class CombatHintsLayer extends AbstractHintsLayer {

    private static final List<String> COMBAT_HINTS = List.of(
            "hold left mouse button and",
            "drag to select units",
            "click right mouse button",
            "to give commands"
    );

    @Override
    protected List<String> getHints() {
        return COMBAT_HINTS;
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return super.shouldRender(hudState) && hudState == HudState.COMBAT
                && !ModUtils.getInfluenceManager().isSelecting();
    }
}
