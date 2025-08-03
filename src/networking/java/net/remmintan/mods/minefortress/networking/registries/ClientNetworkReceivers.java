package net.remmintan.mods.minefortress.networking.registries;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.s2c.*;

import java.util.function.Function;

public class ClientNetworkReceivers {

    public static void registerReceivers() {
        registerReceiver(FortressChannelNames.FINISH_TASK, ClientboundTaskExecutedPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_MANAGER_SYNC, ClientboundSyncFortressManagerPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_SYNC_BLUEPRINT, ClientboundSyncBlueprintPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_REMOVE_BLUEPRINT, ClientboundRemoveBlueprintPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_RESET_BLUEPRINT, ClientboundResetBlueprintPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_BUILDINGS_SYNC, ClientboundSyncBuildingsPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_SYNC, ClientboundProfessionSyncPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_INIT, ClientboundProfessionsInitPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, ClientboundBlueprintsProcessImportExportPacket::new);
        registerReceiver(S2CSyncAreasPacket.CHANNEL, S2CSyncAreasPacket::new);
        registerReceiver(S2CSyncFightManager.CHANNEL, S2CSyncFightManager::new);
        registerReceiver(S2CAddClientTaskPacket.CHANNEL, S2CAddClientTaskPacket::new);
        registerReceiver(S2CSyncGamemodePacket.CHANNEL, S2CSyncGamemodePacket::new);
        registerReceiver(S2CStartFortressConfiguration.CHANNEL, S2CStartFortressConfiguration::new);
        registerReceiver(S2CSyncBuildingScreenInfo.CHANNEL, S2CSyncBuildingScreenInfo::new);
        registerReceiver(S2CSyncItemsState.CHANNEL, S2CSyncItemsState::new);
    }

    private static void registerReceiver(String channelName, Function<PacketByteBuf, FortressS2CPacket> packetConstructor) {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(FortressChannelNames.NAMESPACE, channelName),
                (client, handler, buf, sender) -> {
                    final var packet = packetConstructor.apply(buf);
                    client.execute(() -> packet.handle(client));
                });
    }

}
