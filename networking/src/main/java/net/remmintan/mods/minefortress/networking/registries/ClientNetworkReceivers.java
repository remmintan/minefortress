package net.remmintan.mods.minefortress.networking.registries;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.networking.s2c.S2CSyncAreasPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.networking.s2c.*;

import java.util.function.Function;

public class ClientNetworkReceivers {

    public static void registerReceivers() {
        registerReceiver(FortressChannelNames.FINISH_TASK, ClientboundTaskExecutedPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_MANAGER_SYNC, ClientboundSyncFortressManagerPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_SELECT_COLONIST, ClientboundFollowColonistPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_ADD_BLUEPRINT, ClientboundAddBlueprintPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_UPDATE_BLUEPRINT, ClientboundUpdateBlueprintPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_RESET_BLUEPRINT, ClientboundResetBlueprintPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_BUILDINGS_SYNC, ClientboundSyncBuildingsPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_SPECIAL_BLOCKS_SYNC, ClientboundSyncSpecialBlocksPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_SYNC, ClientboundProfessionSyncPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_RESOURCES_SYNC, ClientboundSyncItemsPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_INIT, ClientboundProfessionsInitPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_BLUEPRINTS_PROCESS_IMPORT_EXPORT, ClientboundBlueprintsProcessImportExportPacket::new);
        registerReceiver(S2COpenHireMenuPacket.CHANNEL, S2COpenHireMenuPacket::new);
        registerReceiver(SyncHireProgress.CHANNEL, SyncHireProgress::new);
        registerReceiver(S2CSyncAreasPacket.CHANNEL, S2CSyncAreasPacket::new);
        registerReceiver(S2CSyncInfluence.CHANNEL, S2CSyncInfluence::new);
        registerReceiver(S2CUpdateInfluenceBorderStage.CHANNEL, S2CUpdateInfluenceBorderStage::new);
        registerReceiver(S2COpenBuildingRepairScreen.CHANNEL, S2COpenBuildingRepairScreen::new);
    }

    private static void registerReceiver(String channelName, Function<PacketByteBuf, FortressS2CPacket> packetConstructor) {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier(FortressChannelNames.NAMESPACE, channelName),
                (client, handler, buf, sender) -> packetConstructor.apply(buf).handle(client, handler.getEnabledFeatures()));
    }

}
