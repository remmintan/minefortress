package org.minefortress.blueprints.manager

import net.minecraft.util.BlockRotation
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRotation

class BlueprintRotation : IBlueprintRotation {

    override var rotation: BlockRotation = BlockRotation.NONE
        private set

    override fun rotateRight() {
        rotation = rotation.rotate(BlockRotation.CLOCKWISE_90)
    }

    override fun rotateLeft() {
        rotation = rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90)
    }

}