package org.minefortress.network.helpers;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.minefortress.network.interfaces.FortressClientPacket;
import org.minefortress.network.interfaces.FortressPacket;

import java.util.function.Function;

public class FortressClientNetworkHelper {

    public static void send(String channelName, FortressPacket packet) {
        final PacketByteBuf packetByteBuf = PacketByteBufs.create();
        packet.write(packetByteBuf);

        ClientPlayNetworking.send(new Identifier(FortressChannelNames.NAMESPACE, channelName), packetByteBuf);
    }

    public static void registerReceiver(String channelName, Function<PacketByteBuf, FortressClientPacket> packetConstructor) {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(FortressChannelNames.NAMESPACE, channelName), (client, handler, buf, sender) -> {
            packetConstructor.apply(buf).handle(client);
        });
    }

}
