package org.minefortress.network;

import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.network.interfaces.FortressServerPacket;

import java.util.Map;

public class ServerboundEditBlueprintPacket implements FortressServerPacket {

    private final String blueprintFileName;

    public ServerboundEditBlueprintPacket(String blueprintFileName) {
        this.blueprintFileName = blueprintFileName;
    }

    public ServerboundEditBlueprintPacket(PacketByteBuf buf) {
        this.blueprintFileName = buf.readString();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.blueprintFileName);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final FortressServer fortressServer = (FortressServer) server;
        final BlueprintsWorld blueprintsWorld = fortressServer.getBlueprintsWorld();
        final ServerWorld world = blueprintsWorld.getWorld();

        final Map<BlockPos, BlockState> blueprintData = fortressServer
                .getBlueprintBlockDataManager()
                .getBlockData(blueprintFileName, BlockRotation.NONE, false)
                .getBlueprintData();

        blueprintsWorld.prepareBlueprint(blueprintData, blueprintFileName);
        blueprintsWorld.putBlueprintInAWorld(player);
        player.moveToWorld(world);
    }
}
