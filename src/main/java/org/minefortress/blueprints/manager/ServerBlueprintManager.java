package org.minefortress.blueprints.manager;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.*;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IInstantTask;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundRemoveBlueprintPacket;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundResetBlueprintPacket;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundSyncBlueprintPacket;
import org.minefortress.blueprints.data.ServerStructureBlockDataManager;
import org.minefortress.tasks.BlueprintDigTask;
import org.minefortress.tasks.BlueprintTask;
import org.minefortress.tasks.InstantPlaceTask;
import org.minefortress.tasks.SimpleSelectionTask;

import java.util.*;

public class ServerBlueprintManager implements IServerBlueprintManager {

    private boolean initialized = false;

    private final ServerStructureBlockDataManager blockDataManager;
    private final BlueprintMetadataReader blueprintMetadataReader;
    private final Queue<FortressS2CPacket> scheduledSyncs = new ArrayDeque<>();

    private final Map<String, BlueprintMetadata> blueprints = new HashMap<>();

    public ServerBlueprintManager(MinecraftServer server) {
        this.blueprintMetadataReader = new BlueprintMetadataReader(server);
        this.blockDataManager = new ServerStructureBlockDataManager(server);
    }

    private static BlockPos getEndPos(BlockPos startPos, Vec3i size) {
        return startPos.add(new Vec3i(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
    }

    @Override
    public void tick(ServerPlayerEntity player) {
        if (!initialized) {
            if (blueprints.isEmpty()) readDefaultBlueprints();

            final ClientboundResetBlueprintPacket resetpacket = new ClientboundResetBlueprintPacket();
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_RESET_BLUEPRINT, resetpacket);

            blueprints.forEach((blueprintId, metadata) -> blockDataManager.getStructureNbt(blueprintId)
                    .ifPresent(it -> {
                        final var packet = new ClientboundSyncBlueprintPacket(metadata, it);
                        scheduledSyncs.add(packet);
                    }));

            initialized = true;
        }

        if (!scheduledSyncs.isEmpty()) {
            final FortressS2CPacket packet = scheduledSyncs.remove();
            if (packet instanceof ClientboundSyncBlueprintPacket)
                FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_SYNC_BLUEPRINT, packet);
            else if (packet instanceof ClientboundRemoveBlueprintPacket)
                FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_REMOVE_BLUEPRINT, packet);
            else
                throw new IllegalStateException("Wrong blueprint update packet type: " + packet.getClass());
        }
    }

    private void readDefaultBlueprints() {
        blueprintMetadataReader.read();
        for (BlueprintMetadata metadata : blueprintMetadataReader.getPredefinedBlueprints()) {
            final String blueprintId = metadata.getId();
            blueprints.put(blueprintId, metadata);
        }
    }

    @Override
    public void remove(String blueprintId) {
        blueprints.remove(blueprintId);
        blockDataManager.remove(blueprintId);
        final var remove = new ClientboundRemoveBlueprintPacket(blueprintId);
        scheduledSyncs.add(remove);
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

    @Override
    public IInstantTask createInstantPlaceTask(String blueprintId, BlockPos start, BlockRotation rotation) {
        final IStructureBlockData serverStructureInfo = blockDataManager.getBlockData(blueprintId, rotation);
        final var blocks = serverStructureInfo.getLayer(BlueprintDataLayer.GENERAL);

        final var metadata = blueprints.get(blueprintId);
        return new InstantPlaceTask(metadata, blocks, start);
    }

    @Override
    public void update(String blueprintId, String blueprintName, BlueprintGroup group, NbtCompound tag, int newFloorLevel) {
        final var obm = blueprints.computeIfAbsent(
                blueprintId,
                id -> new BlueprintMetadata(
                        blueprintName,
                        id,
                        0,
                        10,
                        group
                )
        );
        final var newBlueprintMetadata = new BlueprintMetadata(
                obm.getName(),
                blueprintId,
                newFloorLevel,
                obm.getCapacity(),
                obm.getGroup()
        );

        blueprints.put(blueprintId, newBlueprintMetadata);
        blockDataManager.addOrUpdate(blueprintId, tag);

        final FortressS2CPacket packet = new ClientboundSyncBlueprintPacket(newBlueprintMetadata, tag);
        scheduledSyncs.add(packet);
    }

    @Override
    public NbtCompound write() {
        final var wholeManager = new NbtCompound();

        final var serializedBlueprints = new NbtCompound();
        for (Map.Entry<String, BlueprintMetadata> entry : blueprints.entrySet()) {
            final var blueprintId = entry.getKey();
            final var metadata = entry.getValue();
            serializedBlueprints.put(blueprintId, metadata.toNbt());
        }

        wholeManager.put("blueprints", serializedBlueprints);

        return wholeManager;
    }

    @Override
    public void read(NbtCompound wholeManager) {
        scheduledSyncs.clear();
        blueprints.clear();
        initialized = false;

        if (wholeManager.contains("blueprints")) {
            final var serializedBlueprints = wholeManager.getCompound("blueprints");
            for (String blueprintId : serializedBlueprints.getKeys()) {
                final var metadata = new BlueprintMetadata(serializedBlueprints.getCompound(blueprintId));
                blueprints.put(blueprintId, metadata);
            }
        }
    }
}
