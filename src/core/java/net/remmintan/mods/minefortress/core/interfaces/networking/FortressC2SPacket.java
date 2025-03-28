package net.remmintan.mods.minefortress.core.interfaces.networking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.jetbrains.annotations.NotNull;

public interface FortressC2SPacket extends FortressPacket {

    void handle(@NotNull MinecraftServer server, @NotNull ServerPlayerEntity player);

    default IServerManagersProvider getManagersProvider(ServerPlayerEntity player) {
        return ServerModUtils.getManagersProvider(player).orElseThrow();
    }

    default IServerFortressManager getFortressManager(ServerPlayerEntity player) {
        return ServerModUtils.getFortressManager(player).orElseThrow();
    }

}
