package net.remmintan.mods.minefortress.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata

class FortressBuildingBlockEntity(pos: BlockPos?, state: BlockState?) :
    BlockEntity(FortressBlocks.BUILDING_ENT_TYPE, pos, state), NamedScreenHandlerFactory {

    var blueprintMetadata: BlueprintMetadata? = null
        set(value) {
            if (field != null)
                error("Can't set blueprint metadata twice!")
            field = value
        }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler? {
        TODO("Not yet implemented")
    }

    override fun getDisplayName(): Text {
        val nameStr = blueprintMetadata?.name ?: "Building"
        return Text.of(nameStr)
    }

    override fun readNbt(nbt: NbtCompound?) {
        nbt ?: return
        super.readNbt(nbt)
        this.blueprintMetadata = BlueprintMetadata(nbt.getCompound("blueprintMetadata"))
    }

    override fun writeNbt(nbt: NbtCompound?) {
        nbt ?: return
        super.writeNbt(nbt)
        this.blueprintMetadata?.toNbt()?.let {
            nbt.put("blueprintMetadata", it)
        }
    }
}