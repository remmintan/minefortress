package net.remmintan.mods.minefortress.core.interfaces.client

import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity

interface IFortressCenterManager {

    fun isCenterNotSet(): Boolean
    fun hasTheSameCenter(entity: IFortressAwareEntity): Boolean

    fun tick()

}