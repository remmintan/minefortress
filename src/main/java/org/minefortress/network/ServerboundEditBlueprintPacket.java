package org.minefortress.network;

import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.blueprints.data.BlueprintDataLayer;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;

import java.util.HashMap;
import java.util.Map;

public class ServerboundEditBlueprintPacket implements FortressServerPacket {

    private final Type type;
    private final String blueprintFileName;
    private final int floorLevel;

    private ServerboundEditBlueprintPacket(String blueprintFileName, int floorLevel,  Type type) {
        this.blueprintFileName = blueprintFileName;
        this.floorLevel = floorLevel;
        this.type = type;
    }

    public ServerboundEditBlueprintPacket(PacketByteBuf buf) {
        this.blueprintFileName = buf.readString();
        this.floorLevel = buf.readInt();
        this.type = buf.readEnumConstant(Type.class);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.blueprintFileName);
        buf.writeInt(this.floorLevel);
        buf.writeEnumConstant(type);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(server instanceof FortressServer fortressServer) {
            final BlueprintsWorld blueprintsWorld = fortressServer.getBlueprintsWorld();

            if(player instanceof FortressServerPlayerEntity fortressPlayer) {
                if(type == Type.EDIT) {
                    final var blockData = fortressPlayer
                            .getServerBlueprintManager()
                            .getBlockDataManager()
                            .getBlockData(blueprintFileName, BlockRotation.NONE);
                    final Map<BlockPos, BlockState> blueprintData = blockData
                            .getLayer(BlueprintDataLayer.GENERAL);

                    blueprintsWorld.prepareBlueprint(blueprintData, blueprintFileName, floorLevel);
                    blueprintsWorld.putBlueprintInAWorld(player, blockData.getSize());
                } else if(type == Type.CREATE) {
                    blueprintsWorld.prepareBlueprint(new HashMap<>(), blueprintFileName, floorLevel);
                    blueprintsWorld.putBlueprintInAWorld(player, new Vec3i(1, 1, 1));
                }
                player.moveToWorld(blueprintsWorld.getWorld());
            }
        }
    }

    public static ServerboundEditBlueprintPacket edit(String name, int floorLevel) {
        return new ServerboundEditBlueprintPacket(name, floorLevel, Type.EDIT);
    }

    public static ServerboundEditBlueprintPacket add(String name) {
        return new ServerboundEditBlueprintPacket(name, 0, Type.CREATE);
    }

    private enum Type {
        EDIT, CREATE
    }

}
