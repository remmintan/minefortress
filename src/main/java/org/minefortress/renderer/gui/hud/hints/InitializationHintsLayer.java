package org.minefortress.renderer.gui.hud.hints;

import org.minefortress.renderer.gui.hud.HudState;

import java.util.List;

public class InitializationHintsLayer extends AbstractHintsLayer {

    @Override
    protected List<String> getHints() {
        return List.of(
                "Choose where to place your Fortress",
                "right click - set fortress center",
                "",
                ""
        );
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return super.shouldRender(hudState) && hudState == HudState.INITIALIZING;
    }
}
