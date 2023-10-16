package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

public class C2SCloseHireMenuPacket implements FortressC2SPacket {

    public static final String CHANNEL = "close_hire_menu";

    public C2SCloseHireMenuPacket() {
    }

    public C2SCloseHireMenuPacket(PacketByteBuf buffer) {
    }

    public void write(PacketByteBuf buf) {
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        getManagersProvider(server, player).getProfessionsManager().closeHireMenu();
    }
}
