package org.minefortress.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.data.FortressModDataLoader;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundOpenBlueprintsFolderPacket implements FortressServerPacket {

    public ServerboundOpenBlueprintsFolderPacket() {}
    public ServerboundOpenBlueprintsFolderPacket(PacketByteBuf buf) {}

    @Override
    public void write(PacketByteBuf buf) {

    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) return;

        if(player instanceof FortressServerPlayerEntity fortressServerPlayer) {
            final var blueprintsFolder = fortressServerPlayer.getServerBlueprintManager().getBlockDataManager().getBlueprintsFolder();
            final var folderAbsolutePath = FortressModDataLoader.getFolderAbsolutePath(blueprintsFolder, server.session);
            final var packet = new ClientboundOpenBlueprintsFolderPacket(folderAbsolutePath);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_OPEN_BLUEPRINTS_FOLDER, packet);
        }
    }

}
