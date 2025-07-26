package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.item.ItemStack
import net.remmintan.mods.minefortress.core.dtos.blueprints.BlueprintSlot
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata

interface IInfoTabHandler {
    fun getInfoTabState(): InfoTabState
    val upgrades: List<BlueprintSlot>
    fun getBlueprintMetadata(): BlueprintMetadata
    fun getHealth(): Int
    fun getItemsToRepair(): List<ItemStack>
    fun getEnoughItems(): Map<ItemStack, Boolean>
    fun upgrade(slot: BlueprintSlot)
    fun destroy()
    fun canDestroy(): Boolean
    fun repair()
    fun cancel()
}