package org.minefortress.network.helpers;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.network.interfaces.FortressC2SPacket;
import org.minefortress.network.s2c.*;

import java.util.function.Function;

public class FortressClientNetworkHelper {

    public static void send(String channelName, FortressC2SPacket packet) {
        final PacketByteBuf packetByteBuf = PacketByteBufs.create();
        packet.write(packetByteBuf);
        ClientPlayNetworking.send(new Identifier(FortressChannelNames.NAMESPACE, channelName), packetByteBuf);
    }

    public static void registerReceivers() {
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FINISH_TASK, ClientboundTaskExecutedPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_MANAGER_SYNC, ClientboundSyncFortressManagerPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SELECT_COLONIST, ClientboundFollowColonistPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_ADD_BLUEPRINT, ClientboundAddBlueprintPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_UPDATE_BLUEPRINT, ClientboundUpdateBlueprintPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_RESET_BLUEPRINT, ClientboundResetBlueprintPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_BUILDINGS_SYNC, ClientboundSyncBuildingsPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SPECIAL_BLOCKS_SYNC, ClientboundSyncSpecialBlocksPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_SYNC, ClientboundProfessionSyncPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_RESOURCES_SYNC, ClientboundSyncItemsPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_INIT, ClientboundProfessionsInitPacket::new);
        FortressClientNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, ClientboundBlueprintsProcessImportExportPacket::new);
        FortressClientNetworkHelper.registerReceiver(S2COpenHireMenuPacket.CHANNEL, S2COpenHireMenuPacket::new);
    }

    private static void registerReceiver(String channelName, Function<PacketByteBuf, FortressS2CPacket> packetConstructor) {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(FortressChannelNames.NAMESPACE, channelName),
                (client, handler, buf, sender) -> packetConstructor.apply(buf).handle(client));
    }

}
