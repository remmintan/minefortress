package org.minefortress.blueprints.manager;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.*;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IAreaBasedTask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IPlaceCampfireTask;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundRemoveBlueprintPacket;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundResetBlueprintPacket;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundSyncBlueprintPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.data.ServerStructureBlockDataManager;
import org.minefortress.tasks.AreaBlueprintTask;
import org.minefortress.tasks.PlaceCampfireTask;

import java.util.*;

public class ServerBlueprintManager implements IServerBlueprintManager {

    private boolean initialized = false;

    private ServerStructureBlockDataManager blockDataManager;
    private BlueprintMetadataReader blueprintMetadataReader;
    private final Queue<FortressS2CPacket> scheduledSyncs = new ArrayDeque<>();

    private final Map<String, BlueprintMetadata> blueprints = new HashMap<>();


    @Override
    public BlueprintMetadata get(String blueprintId) {
        return blueprints.get(blueprintId);
    }

    @Override
    public void tick(@NotNull MinecraftServer server, @NotNull ServerWorld world, @Nullable ServerPlayerEntity player) {
        if (blockDataManager == null || blueprintMetadataReader == null) {
            this.blueprintMetadataReader = new BlueprintMetadataReader(server);
            this.blockDataManager = new ServerStructureBlockDataManager(server);
        }
        if (player == null) return;
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
    public IAreaBasedTask createAreaBasedTask(UUID taskId, String blueprintId, BlockPos startPos, BlockRotation rotation, World world) {
        final var metadata = this.get(blueprintId);
        final var structureData = blockDataManager.getBlockData(blueprintId, rotation, metadata.getFloorLevel());
        return new AreaBlueprintTask(
                metadata,
                startPos,
                structureData,
                world
        );
    }

    @Override
    public IPlaceCampfireTask createInstantPlaceTask(String blueprintId, BlockPos start, BlockRotation rotation) {
        final IStructureBlockData serverStructureInfo = blockDataManager.getBlockData(blueprintId, rotation);
        final var blocks = serverStructureInfo.getLayer(BlueprintDataLayer.GENERAL);

        final var metadata = blueprints.get(blueprintId);
        return new PlaceCampfireTask(metadata, blocks, start);
    }

    @Override
    public void update(String blueprintId, String blueprintName, BlueprintGroup group, int newCapacity, NbtCompound tag, int newFloorLevel) {
        final var obm = blueprints.computeIfAbsent(
                blueprintId,
                id -> new BlueprintMetadata(
                        blueprintName,
                        id,
                        0,
                        2, // Default capacity for new blueprints at level 1
                        group
                )
        );
        final var newBlueprintMetadata = new BlueprintMetadata(
                obm.getName(),
                blueprintId,
                newFloorLevel,
                newCapacity,
                obm.getGroup()
        );

        blueprints.put(blueprintId, newBlueprintMetadata);
        blockDataManager.addOrUpdate(blueprintId, tag);

        final FortressS2CPacket packet = new ClientboundSyncBlueprintPacket(newBlueprintMetadata, tag);
        scheduledSyncs.add(packet);
    }

    @Override
    public void write(NbtCompound tag) {
        final var wholeManager = new NbtCompound();

        final var serializedBlueprints = new NbtCompound();
        for (Map.Entry<String, BlueprintMetadata> entry : blueprints.entrySet()) {
            final var blueprintId = entry.getKey();
            final var metadata = entry.getValue();
            serializedBlueprints.put(blueprintId, metadata.toNbt());
        }

        wholeManager.put("blueprints", serializedBlueprints);

        tag.put("blueprintsManager", wholeManager);
    }

    @Override
    public void read(NbtCompound tag) {
        final var wholeManager = tag.getCompound("blueprintsManager");

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
