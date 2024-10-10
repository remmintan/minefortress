package net.remmintan.mods.minefortress.core.interfaces.blueprints

import net.minecraft.item.Item
import net.minecraft.item.Items

enum class BlueprintRequirementType(val icon: Item, val requirementsIds: List<String>) {

    FARMER(Items.WHEAT, listOf("farmer")),
    MINER(Items.STONE_PICKAXE, listOf("miner_wooden", "miner_stone", "miners_guild")),
    LUMBERJACK(Items.STONE_AXE, listOf("lumberjack_wooden", "lumberjack_stone", "lumberjack_guild")),
    BLACKSMITH(Items.FURNACE, listOf("blacksmith")),

}