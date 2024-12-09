package org.minefortress.renderer.gui.hud.hints;

import net.remmintan.mods.minefortress.gui.hud.HudState;

import java.util.List;
import java.util.Optional;

public class BlueprintEditingHintsLayer extends AbstractHintsLayer{
    @Override
    protected List<String> getHints() {
        return List.of("esc - to save changes");
    }

    @Override
    protected Optional<String> getInfoText() {
        return Optional.of("Editing blueprint");
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return super.shouldRender(hudState) && hudState == HudState.BLUEPRINT_EDITING;
    }
}
