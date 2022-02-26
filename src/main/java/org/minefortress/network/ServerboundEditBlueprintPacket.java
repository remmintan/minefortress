package org.minefortress.network;

import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.minefortress.blueprints.data.BlueprintDataLayer;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;

import java.util.Map;

public class ServerboundEditBlueprintPacket implements FortressServerPacket {

    private final String blueprintFileName;
    private final int floorLevel;

    public ServerboundEditBlueprintPacket(String blueprintFileName, int floorLevel) {
        this.blueprintFileName = blueprintFileName;
        this.floorLevel = floorLevel;
    }

    public ServerboundEditBlueprintPacket(PacketByteBuf buf) {
        this.blueprintFileName = buf.readString();
        this.floorLevel = buf.readInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.blueprintFileName);
        buf.writeInt(this.floorLevel);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(server instanceof FortressServer fortressServer) {
            final BlueprintsWorld blueprintsWorld = fortressServer.getBlueprintsWorld();

            if(player instanceof FortressServerPlayerEntity fortressPlayer) {
                final Map<BlockPos, BlockState> blueprintData = fortressPlayer
                        .getServerBlueprintManager()
                        .getBlockDataManager()
                        .getBlockData(blueprintFileName, BlockRotation.NONE)
                        .getLayer(BlueprintDataLayer.GENERAL);

                blueprintsWorld.prepareBlueprint(blueprintData, blueprintFileName, floorLevel);
                blueprintsWorld.putBlueprintInAWorld(player);
                player.moveToWorld(blueprintsWorld.getWorld());
            }
        }
    }
}
