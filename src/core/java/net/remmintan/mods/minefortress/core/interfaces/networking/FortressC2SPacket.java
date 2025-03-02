package net.remmintan.mods.minefortress.core.interfaces.networking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;

public interface FortressC2SPacket extends FortressPacket {

    void handle(MinecraftServer server, ServerPlayerEntity player);

    default IServerManagersProvider getManagersProvider(ServerPlayerEntity player) {
        return ServerModUtils.getManagersProvider(player);
    }

    default IServerFortressManager getFortressManager(ServerPlayerEntity player) {
        return ServerModUtils.getFortressManager(player);
    }

}
