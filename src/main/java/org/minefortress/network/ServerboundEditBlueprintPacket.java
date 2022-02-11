package org.minefortress.network;

import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.network.interfaces.FortressServerPacket;

import java.util.Map;

public class ServerboundEditBlueprintPacket implements FortressServerPacket {

    private final String blueprintFilePath;

    public ServerboundEditBlueprintPacket(String blueprintFilePath) {
        this.blueprintFilePath = blueprintFilePath;
    }

    public ServerboundEditBlueprintPacket(PacketByteBuf buf) {
        this.blueprintFilePath = buf.readString();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.blueprintFilePath);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final FortressServer fortressServer = (FortressServer) server;
        final BlueprintsWorld blueprintsWorld = fortressServer.getBlueprintsWorld();
        final ServerWorld world = blueprintsWorld.getWorld();

        final Map<BlockPos, BlockState> blueprintData = fortressServer
                .getBlueprintBlockDataManager()
                .getBlockData(blueprintFilePath, BlockRotation.NONE, false)
                .getBlueprintData();

        blueprintsWorld.prepareBlueprint(blueprintData);
        blueprintsWorld.putBlueprintInAWorld(player);
        player.moveToWorld(world);
    }
}
