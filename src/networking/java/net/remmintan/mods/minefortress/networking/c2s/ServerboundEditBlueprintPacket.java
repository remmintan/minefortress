package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintDataLayer;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintsWorld;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;

import java.util.HashMap;
import java.util.Map;

public class ServerboundEditBlueprintPacket implements FortressC2SPacket {

    private final Type type;
    private final String blueprintId;
    private final int floorLevel;
    private final BlueprintGroup blueprintGroup;

    private ServerboundEditBlueprintPacket(String blueprintId, int floorLevel, Type type, BlueprintGroup blueprintGroup) {
        this.blueprintId = blueprintId;
        this.floorLevel = floorLevel;
        this.type = type;
        this.blueprintGroup = blueprintGroup;
    }

    public ServerboundEditBlueprintPacket(PacketByteBuf buf) {
        this.blueprintId = buf.readString();
        this.floorLevel = buf.readInt();
        this.type = buf.readEnumConstant(Type.class);
        this.blueprintGroup = buf.readEnumConstant(BlueprintGroup.class);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.blueprintId);
        buf.writeInt(this.floorLevel);
        buf.writeEnumConstant(type);
        buf.writeEnumConstant(blueprintGroup);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(server instanceof IFortressServer fortressServer) {
            if(player instanceof FortressServerPlayerEntity fortressPlayer) {
                if(type == Type.REMOVE) {
                    fortressPlayer.get_ServerBlueprintManager().remove(blueprintId);
                } else {
                    final IBlueprintsWorld blueprintsWorld = fortressServer.get_BlueprintsWorld();
                    if(type == Type.EDIT) {
                        final var blockData = fortressPlayer
                                .get_ServerBlueprintManager()
                                .getBlockDataManager()
                                .getBlockData(blueprintId, BlockRotation.NONE);
                        final Map<BlockPos, BlockState> blueprintData = blockData
                                .getLayer(BlueprintDataLayer.GENERAL);

                        blueprintsWorld.prepareBlueprint(blueprintData, blueprintId, floorLevel, blueprintGroup);
                        blueprintsWorld.putBlueprintInAWorld(player, blockData.getSize());
                    } else if(type == Type.CREATE) {
                        blueprintsWorld.prepareBlueprint(new HashMap<>(), blueprintId, floorLevel, blueprintGroup);
                        blueprintsWorld.putBlueprintInAWorld(player, new Vec3i(1, 1, 1));
                    }
                    player.moveToWorld((ServerWorld) blueprintsWorld.getWorld());
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
