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

    private final String blueprintId;
    private final String blueprintName;
    private final ActionType actionType;
    private final int floorLevel;
    private final BlueprintGroup blueprintGroup;

    private ServerboundEditBlueprintPacket(String blueprintId, String blueprintName, int floorLevel, ActionType actionType, BlueprintGroup blueprintGroup) {
        this.blueprintId = blueprintId;
        this.blueprintName = blueprintName;
        this.floorLevel = floorLevel;
        this.actionType = actionType;
        this.blueprintGroup = blueprintGroup;
    }

    public ServerboundEditBlueprintPacket(PacketByteBuf buf) {
        this.blueprintId = buf.readNullable(PacketByteBuf::readString);
        this.blueprintName = buf.readNullable(PacketByteBuf::readString);
        this.floorLevel = buf.readInt();
        this.blueprintGroup = buf.readEnumConstant(BlueprintGroup.class);
        this.actionType = buf.readEnumConstant(ActionType.class);

    }

    public static ServerboundEditBlueprintPacket edit(String id, int floorLevel, BlueprintGroup group) {
        return new ServerboundEditBlueprintPacket(id, null, floorLevel, ActionType.EDIT, group);
    }

    public static ServerboundEditBlueprintPacket add(String name, BlueprintGroup group) {
        return new ServerboundEditBlueprintPacket(null, name, 0, ActionType.CREATE, group);
    }

    public static ServerboundEditBlueprintPacket remove(String id) {
        return new ServerboundEditBlueprintPacket(id, null, 0, ActionType.REMOVE, BlueprintGroup.LIVING_HOUSES);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeNullable(this.blueprintId, PacketByteBuf::writeString);
        buf.writeNullable(this.blueprintName, PacketByteBuf::writeString);
        buf.writeInt(this.floorLevel);
        buf.writeEnumConstant(blueprintGroup);
        buf.writeEnumConstant(actionType);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(server instanceof IFortressServer fortressServer) {
            if(player instanceof FortressServerPlayerEntity fortressPlayer) {
                if (actionType == ActionType.REMOVE) {
                    fortressPlayer.get_ServerBlueprintManager().remove(blueprintId);
                } else {
                    final IBlueprintsWorld blueprintsWorld = fortressServer.get_BlueprintsWorld();
                    if (actionType == ActionType.EDIT) {
                        final var blockData = fortressPlayer
                                .get_ServerBlueprintManager()
                                .getBlockDataManager()
                                .getBlockData(blueprintId, BlockRotation.NONE);
                        final Map<BlockPos, BlockState> blueprintData = blockData
                                .getLayer(BlueprintDataLayer.GENERAL);

                        blueprintsWorld.prepareBlueprint(blueprintData, blueprintId, blueprintName, floorLevel, blueprintGroup);
                        blueprintsWorld.putBlueprintInAWorld(player, blockData.getSize());
                    } else if (actionType == ActionType.CREATE) {
                        blueprintsWorld.prepareBlueprint(new HashMap<>(), blueprintId, blueprintName, floorLevel, blueprintGroup);
                        blueprintsWorld.putBlueprintInAWorld(player, new Vec3i(1, 1, 1));
                    }
                    player.moveToWorld((ServerWorld) blueprintsWorld.getWorld());
                }
            }
        }
    }

    private enum ActionType {
        EDIT, CREATE, REMOVE
    }

}
