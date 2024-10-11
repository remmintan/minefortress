package net.remmintan.mods.minefortress.core.interfaces.blueprints

import net.minecraft.util.BlockRotation

interface IBlueprintMetadata {

    val name: String?
    val id: String?
    val requirement: IBlueprintRequirement?
    var floorLevel: Int
    val rotation: BlockRotation?

    fun rotateRight()

    fun rotateLeft()

    fun getLevel() = requirement?.level ?: 0


}
