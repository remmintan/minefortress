package net.remmintan.mods.minefortress.core.interfaces.blueprints

import net.minecraft.item.Item
import net.minecraft.item.Items

enum class BlueprintRequirementType(val icon: Item, val displayName: String, val requirementsIds: List<String>) {

    FARMER(Items.WHEAT, "Farmer", listOf("farmer")),
    MINER(Items.STONE_PICKAXE, "Miner", listOf("miner_wooden", "miner_stone", "miners_guild")),
    LUMBERJACK(Items.STONE_AXE, "Lumberjack", listOf("lumberjack_wooden", "lumberjack_stone", "lumberjack_guild")),
    BLACKSMITH(Items.FURNACE, "Blacksmith", listOf("blacksmith")),

}