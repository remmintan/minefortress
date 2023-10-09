package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.resources.server.ServerResourceManager;
import net.remmintan.mods.minefortress.networking.interfaces.FortressC2SPacket;

public class C2SRequestResourcesRefresh implements FortressC2SPacket {

    public static final String CHANNEL = "request_resources_refresh";

    public C2SRequestResourcesRefresh() {}

    public C2SRequestResourcesRefresh(PacketByteBuf ignoredBuf) {}

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var resourceManager = (ServerResourceManager) this
                .getFortressServerManager(server, player)
                .getResourceManager();
        resourceManager.syncAll();
    }

    @Override
    public void write(PacketByteBuf buf) {}
}
