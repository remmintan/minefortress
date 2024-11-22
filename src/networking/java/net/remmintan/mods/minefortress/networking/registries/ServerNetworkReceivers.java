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
        registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_STATE_CHANGE, ServerboundChangeProfessionStatePacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_SET_GAMEMODE, ServerboundSetGamemodePacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_OPEN_CRAFTING_TABLE, ServerboundOpenCraftingScreenPacket::new);
        registerReceiver(FortressChannelNames.SCROLL_CURRENT_SCREEN, ServerboundScrollCurrentScreenPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_SLEEP, ServerboundSleepPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_CHANGE_MAX_COLONISTS_COUNT, ServerboundChangeMaxColonistsCountPacket::new);
        registerReceiver(FortressChannelNames.FORTRESS_BLUEPRINTS_IMPORT_EXPORT, ServerboundBlueprintsImportExportPacket::new);
        registerReceiver(C2SMoveTargetPacket.CHANNEL, C2SMoveTargetPacket::new);
        registerReceiver(C2SFollowTargetPacket.CHANNEL, C2SFollowTargetPacket::new);
        registerReceiver(C2SCloseHireMenuPacket.CHANNEL, C2SCloseHireMenuPacket::new);
        registerReceiver(C2SHirePawnWithScreenPacket.CHANNEL, C2SHirePawnWithScreenPacket::new);
        registerReceiver(C2SAddAreaPacket.CHANNEL, C2SAddAreaPacket::new);
        registerReceiver(C2SRemoveAutomationAreaPacket.CHANNEL, C2SRemoveAutomationAreaPacket::new);
        registerReceiver(C2SCaptureInfluencePositionPacket.CHANNEL, C2SCaptureInfluencePositionPacket::new);
        registerReceiver(C2SUpdateNewInfluencePosition.CHANNEL, C2SUpdateNewInfluencePosition::new);
        registerReceiver(C2SJumpToCampfire.CHANNEL, C2SJumpToCampfire::new);
        registerReceiver(C2SClearActiveBlueprint.CHANNEL, C2SClearActiveBlueprint::new);
        registerReceiver(C2SDestroyBuilding.CHANNEL, C2SDestroyBuilding::new);
        registerReceiver(C2SOpenRepairBuildingScreen.CHANNEL, C2SOpenRepairBuildingScreen::new);
        registerReceiver(C2SRepairBuilding.CHANNEL, C2SRepairBuilding::new);
        registerReceiver(C2SRequestResourcesRefresh.CHANNEL, C2SRequestResourcesRefresh::new);
        registerReceiver(C2SSetNavigationTargetEntity.CHANNEL, C2SSetNavigationTargetEntity::new);
        registerReceiver(C2SAttractWarriorsToCampfire.CHANNEL, C2SAttractWarriorsToCampfire::new);
        registerReceiver(C2SUpdateScreenPropertyKt.getCHANNEL(), C2SUpdateScreenProperty::new);
    }

    private static void registerReceiver(String channelName, Function<PacketByteBuf, FortressC2SPacket> packetConstructor) {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(FortressChannelNames.NAMESPACE, channelName), (server, player, handler, buf, sender) -> {
            final FortressC2SPacket packet = packetConstructor.apply(buf);
            server.execute(() -> packet.handle(server, player));
        });
    }

}
