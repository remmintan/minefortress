package net.remmintan.mods.minefortress.gui.building.handlers

import net.remmintan.mods.minefortress.core.dtos.ItemInfo
import net.remmintan.mods.minefortress.core.dtos.blueprints.BlueprintSlot
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata

interface IInfoTabHandler {
    fun getInfoTabState(): InfoTabState
    val upgrades: List<BlueprintSlot>
    fun getBlueprintMetadata(): BlueprintMetadata
    fun getHealth(): Int
    fun getItemsToRepair(): List<ItemInfo>
    fun hasSelectedPawns(): Boolean
    fun getEnoughItems(): Map<ItemInfo, Boolean>
    fun upgrade(slot: BlueprintSlot)
    fun destroy()
    fun repair()
    fun cancel()
}