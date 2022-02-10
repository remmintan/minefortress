package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundEditBlueprintPacket implements FortressServerPacket {

    private final String blueprintFilePath;

    public ServerboundEditBlueprintPacket(String blueprintFilePath) {
        this.blueprintFilePath = blueprintFilePath;
    }

    public ServerboundEditBlueprintPacket(PacketByteBuf buf) {
        this.blueprintFilePath = buf.readString();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.blueprintFilePath);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final FortressServer fortressServer = (FortressServer) server;
        player.moveToWorld(fortressServer.getBlueprintsWorld().getServerWorld());
    }
}
