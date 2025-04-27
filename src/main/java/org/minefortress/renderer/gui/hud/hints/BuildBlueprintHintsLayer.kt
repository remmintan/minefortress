package org.minefortress.renderer.gui.hud.hints

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IClientBlueprintManager
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.gui.hud.HudState
import org.minefortress.registries.FortressKeybindings.*
import java.util.*

class BuildBlueprintHintsLayer : AbstractHintsLayer() {
    override fun getHints(): List<String> {
        val blueprintManager = ClientModUtils.getBlueprintManager()
        if (blueprintManager.isUpgrading) {
            val upgradeHints = listOf(
                "${getBoundKeyName(moveSelectionDownKeybinding)} - rotate left",
                "${getBoundKeyName(moveSelectionUpKeybinding)} - rotate right"
            )
            if (!blueprintManager.intersectsUpgradingBuilding()) {
                val hints = ArrayList<String>()
                hints.add("The upgrade must intersect with the building!")
                hints.addAll(upgradeHints)
                return hints
            }
            return upgradeHints
        }
        return listOf(
//            "Hold ${getBoundKeyName(releaseCameraKeybinding)} to keep blueprint",
            "${getBoundKeyName(moveSelectionDownKeybinding)} - rotate left",
            "${getBoundKeyName(moveSelectionUpKeybinding)} - rotate right"
        )
    }

    override fun getInfoText(): Optional<String> {
        val bm = blueprintManager
        val bpInfo = bm.selectedStructure.name
        return Optional.of("Blueprint: $bpInfo")
    }

    override fun shouldRender(hudState: HudState): Boolean {
        return super.shouldRender(hudState) && hudState == HudState.BLUEPRINT
    }

    private val blueprintManager: IClientBlueprintManager
        get() = ClientModUtils.getManagersProvider()._BlueprintManager
}
