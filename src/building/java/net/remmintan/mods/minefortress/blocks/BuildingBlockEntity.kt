package net.remmintan.mods.minefortress.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.gui.BuildingConfigurationScreenHandler

class BuildingBlockEntity(pos: BlockPos?, state: BlockState?) :
    BlockEntity(FortressBlocks.BUILDING_ENT_TYPE, pos, state), NamedScreenHandlerFactory {

    var blueprintId: String? = null
    var blueprintName: String? = null
    var blueprintGroup: BlueprintGroup = BlueprintGroup.LIVING_HOUSES
    var capacity: Int = 0
        private set
    var profession: ProfessionType = ProfessionType.NONE
        private set


    private val propertyDelegate = object : PropertyDelegate {
        override fun get(index: Int): Int {
            return when (index) {
                0 -> capacity
                1 -> profession.ordinal
                else -> throw IllegalArgumentException("Invalid property index")
            }
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> capacity = value
                1 -> profession = ProfessionType.entries[value]
            }
        }

        override fun size(): Int = 2
    }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler {
        return BuildingConfigurationScreenHandler(syncId, playerInventory, propertyDelegate)
    }

    override fun getDisplayName(): Text = Text.of("Building configuration")

    override fun writeNbt(nbt: NbtCompound?) {
        super.writeNbt(nbt)
        nbt?.putString("blueprintId", blueprintId)
        if (blueprintName != null) nbt?.putString("blueprintName", blueprintName)
        nbt?.putInt("blueprintGroup", blueprintGroup.ordinal)
    }

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        blueprintId = nbt?.getString("blueprintId")
        blueprintName = if (nbt?.contains("blueprintName") == true) nbt.getString("blueprintName") else null
        blueprintGroup = BlueprintGroup.entries[nbt?.getInt("blueprintGroup") ?: 0]
    }
}