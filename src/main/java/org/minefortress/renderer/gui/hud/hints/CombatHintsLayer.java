package org.minefortress.renderer.gui.hud.hints;

import org.minefortress.renderer.gui.hud.HudState;

import java.util.List;

public class CombatHintsLayer extends AbstractHintsLayer {

    private static final List<String> COMBAT_HINTS = List.of(
            "",
            "",
            "click LMB to select units",
            "click RMB to give commands"
    );

    @Override
    protected List<String> getHints() {
        return COMBAT_HINTS;
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return super.shouldRender(hudState) && hudState == HudState.COMBAT;
    }
}
