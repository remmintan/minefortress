package net.remmintan.mods.minefortress.networking.helpers;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

public class FortressClientNetworkHelper {

    public static void send(String channelName, FortressC2SPacket packet) {
        final PacketByteBuf packetByteBuf = PacketByteBufs.create();
        packet.write(packetByteBuf);
        ClientPlayNetworking.send(new Identifier(FortressChannelNames.NAMESPACE, channelName), packetByteBuf);
    }

}
