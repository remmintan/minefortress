package net.remmintan.mods.minefortress.blocks

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FortressScaffoldBlock : BlockWithEntity(
    FabricBlockSettings
        .create()
        .replaceable()
        .strength(2.0f, 3.0f)
        .sounds(BlockSoundGroup.WOOD)
) {

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity =
        FortressScaffoldBlockEntity(pos, state)

    override fun onPlaced(
        world: World?,
        pos: BlockPos?,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {

        (world?.getBlockEntity(pos) as? FortressScaffoldBlockEntity)?.onPlace(world)
    }

    @Deprecated("Deprecated in Java")
    override fun getRenderType(state: BlockState?): BlockRenderType = BlockRenderType.MODEL

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return if (type == FortressBlocks.SCAFFOLD_ENT_TYPE) {
            BlockEntityTicker { w, p, s, be -> if (be is FortressScaffoldBlockEntity) be.tick(w, p, s) }
        } else {
            null
        }
    }
}