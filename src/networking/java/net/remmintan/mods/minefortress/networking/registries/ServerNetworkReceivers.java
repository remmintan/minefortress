package net.remmintan.mods.minefortress.networking.registries;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.networking.c2s.*;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;

import java.util.function.Function;

public class ServerNetworkReceivers {

    public static void registerReceivers() {
        registerReceiver(FortressChannelNames.NEW_SELECTION_TASK, ServerboundSimpleSelectionTaskPacket::new);
        registerReceiver(FortressChannelNames.NEW_BLUEPRINT_TASK, ServerboundBlueprintTaskPacket::new);
        registerReceiver(FortressChannelNames.CANCEL_TASK, ServerboundCancelTaskPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_EDIT_BLUEPRINT, ServerboundEditBlueprintPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_SAVE_EDIT_BLUEPRINT, ServerboundFinishEditBlueprintPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_CUT_TREES_TASK, ServerboundCutTreesTaskPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_ROADS_TASK, ServerboundRoadsTaskPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_OPEN_CRAFTING_TABLE, ServerboundOpenCraftingScreenPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_SLEEP, ServerboundSleepPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_CHANGE_MAX_COLONISTS_COUNT, ServerboundChangeMaxColonistsCountPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_BLUEPRINTS_IMPORT_EXPORT, ServerboundBlueprintsImportExportPacket::new);
        registerReceiver(C2SMoveTargetPacket.CHANNEL, C2SMoveTargetPacket::new);
        registerReceiver(C2SFollowTargetPacket.CHANNEL, C2SFollowTargetPacket::new);
        registerReceiver(C2SAddAreaPacket.CHANNEL, C2SAddAreaPacket::new);
        registerReceiver(C2SRemoveAutomationAreaPacket.CHANNEL, C2SRemoveAutomationAreaPacket::new);
        registerReceiver(C2SJumpToCampfire.CHANNEL, C2SJumpToCampfire::new);
        registerReceiver(C2SClearActiveBlueprint.CHANNEL, C2SClearActiveBlueprint::new);
        registerReceiver(C2SDestroyBuilding.CHANNEL, C2SDestroyBuilding::new);
        registerReceiver(C2SRepairBuilding.CHANNEL, C2SRepairBuilding::new);
        registerReceiver(C2SSetNavigationTargetEntity.CHANNEL, C2SSetNavigationTargetEntity::new);
        registerReceiver(C2SAttractWarriorsToCampfire.CHANNEL, C2SAttractWarriorsToCampfire::new);
        registerReceiver(C2SUpdateScreenProperty.CHANNEL, C2SUpdateScreenProperty::new);
        registerReceiver(C2SOpenBuildingScreen.CHANNEL, C2SOpenBuildingScreen::new);
        registerReceiver(C2SHireProfessional.CHANNEL, C2SHireProfessional::new);
        registerReceiver(C2SOpenBuildingHireScreen.CHANNEL, C2SOpenBuildingHireScreen::new);
        registerReceiver(C2SSwitchToMinecraftSurvival.CHANNEL, C2SSwitchToMinecraftSurvival::new);
        registerReceiver(C2SSetupCampfirePacket.CHANNEL, C2SSetupCampfirePacket::new);
        registerReceiver(C2SSwitchToFortressModePacket.CHANNEL, C2SSwitchToFortressModePacket::new);
        registerReceiver(C2SClientReadyPacket.CHANNEL, C2SClientReadyPacket::new);
        registerReceiver(C2SSetPawnSkinPacket.CHANNEL, C2SSetPawnSkinPacket::new);
    }

    private static void registerReceiver(String channelName, Function<PacketByteBuf, FortressC2SPacket> packetConstructor) {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(FortressChannelNames.NAMESPACE, channelName), (server, player, handler, buf, sender) -> {
            if (player == null || server == null)
                throw new IllegalArgumentException("Player and server must not be null in the server packet receiver");
            final FortressC2SPacket packet = packetConstructor.apply(buf);
            server.execute(() -> packet.handle(server, player));
        });
    }

}
