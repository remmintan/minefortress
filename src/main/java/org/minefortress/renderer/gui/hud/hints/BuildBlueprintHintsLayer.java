package org.minefortress.renderer.gui.hud.hints;

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.gui.hud.HudState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BuildBlueprintHintsLayer extends AbstractHintsLayer {

    private static final List<String> BUILD_HINTS = List.of(
            "hold ctrl - keep blueprint",
            "ctrl + Q - rotate left",
            "ctrl + E - rotate right"
    );

    private static final List<String> UPGRADING_HINTS = List.of(
            "ctrl + Q - rotate left",
            "ctrl + E - rotate right"
    );

    @Override
    protected List<String> getHints() {
        final var blueprintManager = ClientModUtils.getBlueprintManager();
        if (blueprintManager.isUpgrading()) {
            if (!blueprintManager.intersectsUpgradingBuilding()) {
                final var hints = new ArrayList<String>();
                hints.add("The upgrade must intersect with the building!");
                hints.addAll(UPGRADING_HINTS);
                return hints;
            }
            return UPGRADING_HINTS;
        }
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
        return ClientModUtils.getManagersProvider().get_BlueprintManager();
    }
}
