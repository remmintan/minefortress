package org.minefortress.fortress

import net.minecraft.client.MinecraftClient
import net.remmintan.mods.minefortress.core.interfaces.client.IFortressCenterManager
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity
import net.remmintan.mods.minefortress.core.utils.ClientModUtils

class FortressCenterManager : IFortressCenterManager {

    override fun isCenterNotSet(): Boolean {
        return getFortressPlayer().get_FortressPos().isEmpty
    }

    override fun hasTheSameCenter(entity: IFortressAwareEntity): Boolean {
        return getFortressPlayer().get_FortressPos().map { it.equals(entity.fortressPos) }.orElse(false)
    }

    override fun tick() {
        val blueprintManager = ClientModUtils.getManagersProvider()._BlueprintManager
        if (!blueprintManager.isSelecting || blueprintManager.selectedStructure.id != "campfire") {
            blueprintManager.select("campfire")
        }
    }

    private fun getFortressPlayer(): IFortressPlayerEntity {
        return MinecraftClient.getInstance().player as? IFortressPlayerEntity ?: error("There is no player")
    }

}