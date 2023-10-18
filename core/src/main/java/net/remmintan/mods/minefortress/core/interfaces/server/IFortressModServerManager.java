package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public interface IFortressModServerManager {

    IServerManagersProvider getManagersProvider(ServerPlayerEntity player);

    IServerManagersProvider getManagersProvider(UUID id);

    IServerFortressManager getFortressManager(ServerPlayerEntity player);

    IServerFortressManager getFortressManager(UUID id);

}
