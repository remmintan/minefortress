package net.remmintan.mods.minefortress.core.interfaces.server

import net.minecraft.server.network.ServerPlayerEntity

interface IFortressHolder {

    fun getServerFortressManager(): IServerFortressManager
    fun getServerManagersProvider(): IServerManagersProvider
    fun getFortressOwner(): ServerPlayerEntity?

}