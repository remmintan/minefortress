package org.minefortress.network.c2s;

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
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;

import java.util.HashMap;
import java.util.Map;

public class ServerboundEditBlueprintPacket implements FortressServerPacket {

    private final Type type;
    private final String blueprintFileName;
    private final int floorLevel;
    private final BlueprintGroup blueprintGroup;

    private ServerboundEditBlueprintPacket(String blueprintFileName, int floorLevel,  Type type, BlueprintGroup blueprintGroup) {
        this.blueprintFileName = blueprintFileName;
        this.floorLevel = floorLevel;
        this.type = type;
        this.blueprintGroup = blueprintGroup;
    }

    public ServerboundEditBlueprintPacket(PacketByteBuf buf) {
        this.blueprintFileName = buf.readString();
        this.floorLevel = buf.readInt();
        this.type = buf.readEnumConstant(Type.class);
        this.blueprintGroup = buf.readEnumConstant(BlueprintGroup.class);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.blueprintFileName);
        buf.writeInt(this.floorLevel);
        buf.writeEnumConstant(type);
        buf.writeEnumConstant(blueprintGroup);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(server instanceof FortressServer fortressServer) {
            if(player instanceof FortressServerPlayerEntity fortressPlayer) {
                if(type == Type.REMOVE) {
                    fortressPlayer.getServerBlueprintManager().remove(blueprintFileName);
                } else {
                    final BlueprintsWorld blueprintsWorld = fortressServer.getBlueprintsWorld();
                    if(type == Type.EDIT) {
                        final var blockData = fortressPlayer
                                .getServerBlueprintManager()
                                .getBlockDataManager()
                                .getBlockData(blueprintFileName, BlockRotation.NONE);
                        final Map<BlockPos, BlockState> blueprintData = blockData
                                .getLayer(BlueprintDataLayer.GENERAL);

                        blueprintsWorld.prepareBlueprint(blueprintData, blueprintFileName, floorLevel, blueprintGroup);
                        blueprintsWorld.putBlueprintInAWorld(player, blockData.getSize());
                    } else if(type == Type.CREATE) {
                        blueprintsWorld.prepareBlueprint(new HashMap<>(), blueprintFileName, floorLevel, blueprintGroup);
                        blueprintsWorld.putBlueprintInAWorld(player, new Vec3i(1, 1, 1));
                    }
                    player.moveToWorld(blueprintsWorld.getWorld());
                }
            }
        }
    }

    public static ServerboundEditBlueprintPacket edit(String name, int floorLevel, BlueprintGroup group) {
        return new ServerboundEditBlueprintPacket(name, floorLevel, Type.EDIT, group);
    }

    public static ServerboundEditBlueprintPacket add(String name, BlueprintGroup group) {
        return new ServerboundEditBlueprintPacket(name, 0, Type.CREATE, group);
    }

    public static ServerboundEditBlueprintPacket remove(String name) {
        return new ServerboundEditBlueprintPacket(name, 0, Type.REMOVE, BlueprintGroup.LIVING_HOUSES);
    }

    private enum Type {
        EDIT, CREATE, REMOVE
    }

}
