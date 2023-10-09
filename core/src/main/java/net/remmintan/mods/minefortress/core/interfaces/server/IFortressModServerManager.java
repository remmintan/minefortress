package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public interface IFortressModServerManager {

    IServerManagersProvider getByPlayer(ServerPlayerEntity player);

    IServerManagersProvider getByPlayerId(UUID id);

}
