package net.remmintan.mods.minefortress.core.interfaces.entities.player

import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.dtos.SupportLevel
import java.util.*

interface IFortressPlayerEntity {

    fun get_FortressPos(): Optional<BlockPos>
    fun set_FortressPos(pos: BlockPos?)
    fun get_SupportLevel(): SupportLevel
    fun set_SupportLevel(level: SupportLevel)

}