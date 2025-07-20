package net.remmintan.mods.minefortress.blocks.task

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.blocks.FortressBlocks

private const val INVENTORY_ITEMS_NBT_KEY = "invItems"

class FortressTaskBlockEntity @JvmOverloads constructor(
    pos: BlockPos?,
    state: BlockState?,
    private val inv: SimpleInventory = SimpleInventory(16)
) :
    BlockEntity(FortressBlocks.TASK_ENT_TYPE, pos, state), Inventory by inv {

    override fun markDirty() {
        super.markDirty()
        inv.markDirty()
    }

    override fun writeNbt(nbt: NbtCompound) {
        val invItems = inv.toNbtList()
        nbt.put(INVENTORY_ITEMS_NBT_KEY, invItems)
    }

    override fun readNbt(nbt: NbtCompound) {
        if (nbt.contains(INVENTORY_ITEMS_NBT_KEY)) {
            val itemsNbtList = nbt.getList(INVENTORY_ITEMS_NBT_KEY, NbtElement.COMPOUND_TYPE.toInt())
            inv.readNbtList(itemsNbtList)
        }
    }
}