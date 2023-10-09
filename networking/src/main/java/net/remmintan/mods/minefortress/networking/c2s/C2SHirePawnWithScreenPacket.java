package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.networking.interfaces.FortressC2SPacket;

public class C2SHirePawnWithScreenPacket implements FortressC2SPacket {

    public static final String CHANNEL = "hire_pawn_with_screen";

    private final String professionId;

    public C2SHirePawnWithScreenPacket(String professionId) {
        this.professionId = professionId;
    }

    public C2SHirePawnWithScreenPacket(PacketByteBuf buffer) {
        this.professionId = buffer.readString();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(professionId);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        getManagersProvider(server, player)
                .getProfessionsManager()
                .sendHireRequestToCurrentHandler(professionId);
    }

}
