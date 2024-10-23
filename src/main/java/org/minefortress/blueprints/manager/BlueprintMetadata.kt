package org.minefortress.blueprints.manager

import net.minecraft.util.BlockRotation
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadata
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement

class BlueprintMetadata(
    override val name: String,
    override val id: String,
    override var floorLevel: Int,
) : IBlueprintMetadata {

    override val requirement: IBlueprintRequirement = BlueprintRequirement(id)
    override var rotation: BlockRotation = BlockRotation.NONE
        private set

    override fun rotateRight() {
        rotation = rotation.rotate(BlockRotation.CLOCKWISE_90)
    }

    override fun rotateLeft() {
        rotation = rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)
    }
}
