package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;

public interface IServerManager {

    default IServerManagersProvider getManagersProvider(ServerPlayerEntity player) {
        return CoreModUtils.getManagersProvider(player);
    }

    default IServerFortressManager getFortressManager(ServerPlayerEntity player) {
        return CoreModUtils.getFortressManager(player);
    }

}
