package net.remmintan.mods.minefortress.core.interfaces.blueprints

import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.StringIdentifiable

enum class ProfessionType(val icon: Item, val displayName: String, val blueprintIds: List<String>) :
    StringIdentifiable {

    NONE(Items.AIR, "None", emptyList()),
    FARMER(Items.WHEAT, "Farmer", listOf("small_farm_1", "large_farm_1")),
    MINER(Items.STONE_PICKAXE, "Miner", listOf("miner_house_wooden", "miner_house_stone", "miner_house_guild")),
    LUMBERJACK(
        Items.STONE_AXE,
        "Lumberjack",
        listOf("lumberjack_house_wooden", "lumberjack_house_stone", "lumberjack_house_guild")
    ),
    BLACKSMITH(Items.FURNACE, "Blacksmith", listOf("small_house_3_with_furnace", "weaponsmith_1")),
    FORESTER(Items.APPLE, "Forester", listOf("forester_house")),
    WARRIOR(Items.STONE_SWORD, "Warrior", listOf("warrior_1", "warrior_2")),
    ARCHER(Items.BOW, "Archer", listOf("shooting_gallery")),
    FISHERMAN(Items.FISHING_ROD, "Fisher", listOf("fisher_cottage_1")),
    CRAFTSMAN(Items.CRAFTING_TABLE, "Craftsman", listOf("campfire"));

    override fun asString() = name.lowercase()
}