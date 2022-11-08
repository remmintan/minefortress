package org.minefortress.network.helpers;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.minefortress.network.c2s.*;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.network.interfaces.FortressC2SPacket;

import java.util.function.Function;

public class FortressServerNetworkHelper {

    public static void send(ServerPlayerEntity player, String channelName, FortressS2CPacket packet) {
        final PacketByteBuf packetByteBuf = PacketByteBufs.create();
        packet.write(packetByteBuf);

        ServerPlayNetworking.send(player, new Identifier(FortressChannelNames.NAMESPACE, channelName), packetByteBuf);
    }

    private static void registerReceiver(String channelName, Function<PacketByteBuf, FortressC2SPacket> packetConstructor) {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(FortressChannelNames.NAMESPACE, channelName), (server, player, handler, buf, sender) -> {
            final FortressC2SPacket packet = packetConstructor.apply(buf);
            server.execute(() -> packet.handle(server, player));
        });
    }

    public static void registerReceivers() {
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.NEW_SELECTION_TASK, ServerboundSimpleSelectionTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.NEW_BLUEPRINT_TASK, ServerboundBlueprintTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.CANCEL_TASK, ServerboundCancelTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SET_CENTER, ServerboundFortressCenterSetPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_EDIT_BLUEPRINT, ServerboundEditBlueprintPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SAVE_EDIT_BLUEPRINT, ServerboundFinishEditBlueprintPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_CUT_TREES_TASK, ServerboundCutTreesTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_ROADS_TASK, ServerboundRoadsTaskPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_PROFESSION_STATE_CHANGE, ServerboundChangeProfessionStatePacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SET_GAMEMODE, ServerboundSetGamemodePacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_OPEN_CRAFTING_TABLE, ServerboundOpenCraftingScreenPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.SCROLL_CURRENT_SCREEN, ServerboundScrollCurrentScreenPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_SLEEP, ServerboundSleepPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_CHANGE_MAX_COLONISTS_COUNT, ServerboundChangeMaxColonistsCountPacket::new);
        FortressServerNetworkHelper.registerReceiver(FortressChannelNames.FORTRESS_BLUEPRINTS_IMPORT_EXPORT, ServerboundBlueprintsImportExportPacket::new);
    }

}
