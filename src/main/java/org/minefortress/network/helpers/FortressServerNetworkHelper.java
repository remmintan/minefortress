package org.minefortress.network.helpers;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.minefortress.network.interfaces.FortressPacket;
import org.minefortress.network.interfaces.FortressServerPacket;

import java.util.function.Function;

public class FortressServerNetworkHelper {

    public static void send(ServerPlayerEntity player, String channelName, FortressPacket packet) {
        final PacketByteBuf packetByteBuf = PacketByteBufs.create();
        packet.write(packetByteBuf);

        ServerPlayNetworking.send(player, new Identifier(FortressChannelNames.NAMESPACE, channelName), packetByteBuf);
    }

    public static void registerReceiver(String channelName, Function<PacketByteBuf, FortressServerPacket> packetConstructor) {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(FortressChannelNames.NAMESPACE, channelName), (server, player, handler, buf, sender) -> {
            packetConstructor.apply(buf).handle(server, player);
        });
    }

}
