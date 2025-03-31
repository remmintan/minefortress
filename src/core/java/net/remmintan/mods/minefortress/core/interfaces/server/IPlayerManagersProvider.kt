package net.remmintan.mods.minefortress.core.interfaces.server

import net.remmintan.mods.minefortress.core.interfaces.blueprints.IServerBlueprintManager

interface IPlayerManagersProvider {

    fun get_BlueprintManager(): IServerBlueprintManager

}