package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

public class ServerboundSetGamemodePacket implements FortressC2SPacket {

    private final FortressGamemode fortressGamemode;

    public ServerboundSetGamemodePacket(FortressGamemode fortressGamemode) {
        this.fortressGamemode = fortressGamemode;
    }

    public ServerboundSetGamemodePacket(PacketByteBuf buf) {
        this.fortressGamemode = FortressGamemode.valueOf(buf.readString());
    }


    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.fortressGamemode.name());
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServerManager = getFortressManager(player);
        fortressServerManager.setGamemode(fortressGamemode);
    }
}
