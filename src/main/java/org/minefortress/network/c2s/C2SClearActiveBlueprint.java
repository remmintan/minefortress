package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.network.interfaces.FortressC2SPacket;

public class C2SClearActiveBlueprint implements FortressC2SPacket {

    public static final String CHANNEL = "clear_active_blueprint";

    public C2SClearActiveBlueprint() {
    }
    public C2SClearActiveBlueprint(PacketByteBuf buf) {
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        var fortressServer = (FortressServer) server;
        fortressServer.get_BlueprintsWorld().clearBlueprint(player);
    }

    @Override
    public void write(PacketByteBuf buf) {

    }
}
