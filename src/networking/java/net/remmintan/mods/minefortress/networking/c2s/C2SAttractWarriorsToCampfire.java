package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

public class C2SAttractWarriorsToCampfire implements FortressC2SPacket {

    public static final String CHANNEL = "attract_warriors_to_campfire";

    public C2SAttractWarriorsToCampfire() {}
    public C2SAttractWarriorsToCampfire(PacketByteBuf ignored) {}

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fightManager = getManagersProvider(player).getFightManager();
        player.sendMessage(Text.literal("Attracting warriors to campfire..."), false);
        fightManager.attractWarriorsToCampfire();
    }

    @Override
    public void write(PacketByteBuf buf) {}
}
