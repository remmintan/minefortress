package org.minefortress.blueprints.manager;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.*;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundAddBlueprintPacket;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundResetBlueprintPacket;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundUpdateBlueprintPacket;
import org.minefortress.blueprints.data.ServerStructureBlockDataManager;
import org.minefortress.tasks.BlueprintDigTask;
import org.minefortress.tasks.BlueprintTask;
import org.minefortress.tasks.SimpleSelectionTask;

import java.util.*;
import java.util.function.Supplier;

public class ServerBlueprintManager implements IServerBlueprintManager {

    private boolean initialized = false;

    private final ServerStructureBlockDataManager blockDataManager;
    private final BlueprintMetadataReader blueprintMetadataReader;
    private final Queue<FortressS2CPacket> scheduledEdits = new ArrayDeque<>();

    public ServerBlueprintManager(MinecraftServer server, Supplier<UUID> userIdProvider) {
        this.blueprintMetadataReader = new BlueprintMetadataReader(server);
        this.blockDataManager = new ServerStructureBlockDataManager(server, blueprintMetadataReader::convertIdToGroup, userIdProvider);
    }

    @Override
    public void tick(ServerPlayerEntity player) {
        if(!initialized) {
            blueprintMetadataReader.read();
            scheduledEdits.clear();
            final ClientboundResetBlueprintPacket resetpacket = new ClientboundResetBlueprintPacket();
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_RESET_BLUEPRINT, resetpacket);

            for(Map.Entry<BlueprintGroup, List<IBlueprintMetadata>> entry : blueprintMetadataReader.getPredefinedBlueprints().entrySet()) {
                for(IBlueprintMetadata blueprintMetadata : entry.getValue()) {
                    final String file = blueprintMetadata.getId();
                    blockDataManager.getStructureNbt(file)
                            .ifPresent(it -> {
                                final int floorLevel = blockDataManager.getFloorLevel(file).orElse(blueprintMetadata.getFloorLevel());
                                final ClientboundAddBlueprintPacket packet = new ClientboundAddBlueprintPacket(
                                        entry.getKey(),
                                        blueprintMetadata.getName(),
                                        file,
                                        floorLevel,
                                        blueprintMetadata.getCapacity(),
                                        it
                                );
                                FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_ADD_BLUEPRINT, packet);
                            });
                }
            }

            final var initPackets = blockDataManager.getInitPackets();
            scheduledEdits.addAll(initPackets);

            initialized = true;
        }

        if(!scheduledEdits.isEmpty()) {
            final FortressS2CPacket packet = scheduledEdits.remove();
            if(packet instanceof ClientboundUpdateBlueprintPacket)
                FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_UPDATE_BLUEPRINT, packet);
            else if(packet instanceof ClientboundAddBlueprintPacket)
                FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_ADD_BLUEPRINT, packet);
            else
                throw new IllegalStateException("Wrong blueprint update packet type: " + packet.getClass());
        }
    }

    @Override
    public void update(String blueprintId, NbtCompound updatedStructure, int newFloorLevel, int capacity, BlueprintGroup group) {
        final var existed = blockDataManager.update(blueprintId, updatedStructure, newFloorLevel, capacity, group);
        final FortressS2CPacket packet =
                existed ? ClientboundUpdateBlueprintPacket.edit(blueprintId, newFloorLevel, updatedStructure) :
                        new ClientboundAddBlueprintPacket(group, blueprintId, blueprintId, newFloorLevel, capacity, updatedStructure);
        scheduledEdits.add(packet);
    }

    @Override
    public void remove(String blueprintId) {
        blockDataManager.remove(blueprintId);
        final var remove = ClientboundUpdateBlueprintPacket.remove(blueprintId);
        scheduledEdits.add(remove);
    }

    @Override
    public IServerStructureBlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    @Override
    public BlueprintTask createTask(UUID taskId, String blueprintId, BlockPos startPos, BlockRotation rotation, int floorLevel) {
        final IStructureBlockData serverStructureInfo = blockDataManager.getBlockData(blueprintId, rotation, floorLevel);
        final Vec3i size = serverStructureInfo.getSize();
        startPos = startPos.down(floorLevel);
        final BlockPos endPos = getEndPos(startPos, size);
        final Map<BlockPos, BlockState> manualLayer = serverStructureInfo.getLayer(BlueprintDataLayer.MANUAL);
        final Map<BlockPos, BlockState> automatic = serverStructureInfo.getLayer(BlueprintDataLayer.AUTOMATIC);
        final Map<BlockPos, BlockState> entityLayer = serverStructureInfo.getLayer(BlueprintDataLayer.ENTITY);
        return new BlueprintTask(
                taskId,
                startPos,
                endPos,
                manualLayer,
                automatic,
                entityLayer,
                floorLevel,
                blueprintId
        );
    }

    @Override
    public SimpleSelectionTask createDigTask(UUID taskId, BlockPos startPos, int floorLevel, String blueprintId, BlockRotation rotation) {
        final IStructureBlockData serverStructureInfo = blockDataManager.getBlockData(blueprintId, rotation);
        final Vec3i size = serverStructureInfo.getSize();
        startPos = startPos.down(floorLevel);
        final BlockPos endPos = getEndPos(startPos, size);

        return new BlueprintDigTask(taskId, startPos, endPos);
    }

    private static BlockPos getEndPos(BlockPos startPos, Vec3i size) {
        return startPos.add(new Vec3i(size.getX()-1, size.getY()-1, size.getZ()-1));
    }

    @Override
    public void write() {
        blockDataManager.writeBlockDataManager();
    }

    @Override
    public void read() {
        blockDataManager.readBlockDataManager();
        initialized = false;
    }
}
