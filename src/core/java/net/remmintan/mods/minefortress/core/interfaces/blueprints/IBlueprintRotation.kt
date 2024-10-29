package net.remmintan.mods.minefortress.core.interfaces.blueprints

import net.minecraft.util.BlockRotation

interface IBlueprintRotation {

    val rotation: BlockRotation?

    fun rotateRight()

    fun rotateLeft()

}