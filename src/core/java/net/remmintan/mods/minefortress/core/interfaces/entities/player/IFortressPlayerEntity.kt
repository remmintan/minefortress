package net.remmintan.mods.minefortress.core.interfaces.entities.player

import net.minecraft.util.math.BlockPos
import java.util.*

interface IFortressPlayerEntity {

    fun get_FortressPos(): Optional<BlockPos>
    fun set_FortressPos(pos: BlockPos?)

}