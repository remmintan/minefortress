package org.minefortress.entity

import net.minecraft.world.World
import net.remmintan.mods.minefortress.core.dtos.PawnSkin
import org.minefortress.registries.FortressEntities

class FakeColonistSkinPreview(world: World?) : Colonist(FortressEntities.COLONIST_ENTITY_TYPE, world) {

    private var skin: PawnSkin = PawnSkin.VILLAGER

    override var pawnSkin: PawnSkin
        get() = skin
        set(value) {
            skin = value
        }
}