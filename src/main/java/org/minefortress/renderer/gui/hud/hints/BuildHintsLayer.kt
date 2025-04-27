package org.minefortress.renderer.gui.hud.hints

import net.remmintan.gobi.SelectionType
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.gui.hud.HudState
import org.minefortress.registries.FortressKeybindings.*
import java.util.*


class BuildHintsLayer : AbstractHintsLayer() {
    override fun shouldRender(hudState: HudState): Boolean {
        val selectType = selectionManager.currentSelectionType
        return super.shouldRender(hudState) && hudState == HudState.BUILD && selectType !== SelectionType.TREE && selectType !== SelectionType.ROADS
    }

    override fun getHints(): List<String> {
        return if (selectionManager.isSelecting) {
            if (selectionManager.clickType == ClickType.REMOVE) {
                listOf(
                    "left click - confirm task",
                    "right click - cancel",
                    "${getBoundKeyName(moveSelectionUpKeybinding)} - move up",
                    "${getBoundKeyName(moveSelectionDownKeybinding)} - move down"
                )
            } else {
                listOf(
                    "left click - cancel",
                    "right click - confirm task",
                    "${getBoundKeyName(moveSelectionUpKeybinding)} - move up",
                    "${getBoundKeyName(moveSelectionDownKeybinding)} - move down"
                )
            }
        } else {
            listOf(
                "${getBoundKeyName(releaseCameraKeybinding)} - look around",
                "left click - dig",
                "right click - build"
            )
        }
    }

    override fun getInfoText(): Optional<String> {
        val name = selectionManager.currentSelectionType.displayName
        return Optional.of("Selection type: $name")
    }

    private val selectionManager: ISelectionManager
        get() = ClientModUtils.getManagersProvider()._SelectionManager


}
