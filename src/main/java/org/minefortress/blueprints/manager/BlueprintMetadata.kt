package org.minefortress.blueprints.manager

import net.minecraft.util.BlockRotation
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadata
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement
import java.util.*

class BlueprintMetadata(
    override val name: String,
    override val id: String,
    override var floorLevel: Int,
    requirementId: String?
) : IBlueprintMetadata {
    override val requirement: IBlueprintRequirement =
        BlueprintRequirement(Optional.ofNullable(requirementId).orElse("none"))

    override var rotation: BlockRotation = BlockRotation.NONE
        private set

    override fun rotateRight() {
        rotation = rotation.rotate(BlockRotation.CLOCKWISE_90)
    }

    override fun rotateLeft() {
        rotation = rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)
    }
}
