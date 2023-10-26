package org.minefortress.renderer.gui.hud.hints;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.minefortress.renderer.gui.hud.HudState;

import java.util.List;
import java.util.Optional;

public class BuildBlueprintHintsLayer extends AbstractHintsLayer {

    private static final List<String> BUILD_HINTS = List.of(
            "hold ctrl - keep blueprint",
            "ctrl + R - next blueprint",
            "ctrl + Q - rotate left",
            "ctrl + E - rotate right"
    );

    @Override
    protected List<String> getHints() {
        return BUILD_HINTS;
    }

    @Override
    protected Optional<String> getInfoText() {
        final var bm = getBlueprintManager();
        final var bpInfo = bm.getSelectedStructure().getName();
        return Optional.of("Blueprint: " + bpInfo);
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return super.shouldRender(hudState) && hudState == HudState.BLUEPRINT;
    }

    private IClientBlueprintManager getBlueprintManager() {
        return CoreModUtils.getMineFortressManagersProvider().get_BlueprintManager();
    }
}
