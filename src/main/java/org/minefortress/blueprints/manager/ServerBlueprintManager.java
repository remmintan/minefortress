package org.minefortress.blueprints.manager;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.blueprints.data.BlueprintBlockData;
import org.minefortress.blueprints.data.BlueprintDataLayer;
import org.minefortress.blueprints.data.ServerBlueprintBlockDataManager;
import org.minefortress.network.ClientboundAddBlueprintPacket;
import org.minefortress.network.ClientboundResetBlueprintPacket;
import org.minefortress.network.ClientboundUpdateBlueprintPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.interfaces.FortressClientPacket;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;
import org.minefortress.tasks.BlueprintDigTask;
import org.minefortress.tasks.BlueprintTask;
import org.minefortress.tasks.SimpleSelectionTask;

import java.util.*;
import java.util.function.Supplier;

public class ServerBlueprintManager {

    private boolean initialized = false;

    private final ServerBlueprintBlockDataManager blockDataManager;
    private final BlueprintMetadataReader blueprintMetadataReader;
    private final Queue<FortressClientPacket> scheduledEdits = new ArrayDeque<>();

    public ServerBlueprintManager(MinecraftServer server, Supplier<UUID> userIdProvider) {
        this.blueprintMetadataReader = new BlueprintMetadataReader(server);
        this.blockDataManager = new ServerBlueprintBlockDataManager(server, blueprintMetadataReader::convertFilenameToGroup, userIdProvider);
    }

    public void tick(ServerPlayerEntity player) {
        if(!initialized) {
            blueprintMetadataReader.read();
            scheduledEdits.clear();
            final ClientboundResetBlueprintPacket resetpacket = new ClientboundResetBlueprintPacket();
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_RESET_BLUEPRINT, resetpacket);

            for(Map.Entry<BlueprintGroup, List<BlueprintMetadata>> entry : blueprintMetadataReader.getPredefinedBlueprints().entrySet()) {
                for(BlueprintMetadata blueprintMetadata : entry.getValue()) {
                    final String file = blueprintMetadata.getFile();
                    blockDataManager.getStructureNbt(file)
                            .ifPresent(it -> {
                                final int floorLevel = blockDataManager.getFloorLevel(file).orElse(blueprintMetadata.getFloorLevel());
                                final ClientboundAddBlueprintPacket packet = new ClientboundAddBlueprintPacket(
                                        entry.getKey(),
                                        blueprintMetadata.getName(),
                                        file,
                                        floorLevel,
                                        blueprintMetadata.getRequirementId(),
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
            final FortressClientPacket packet = scheduledEdits.remove();
            if(packet instanceof ClientboundUpdateBlueprintPacket)
                FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_UPDATE_BLUEPRINT, packet);
            else if(packet instanceof ClientboundAddBlueprintPacket)
                FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_ADD_BLUEPRINT, packet);
            else
                throw new IllegalStateException("Wrong blueprint update packet type: " + packet.getClass());
        }
    }

    public void update(String fileName, NbtCompound updatedStructure, int newFloorLevel, BlueprintGroup group) {
        final var existed = blockDataManager.update(fileName, updatedStructure, newFloorLevel, group);
        final FortressClientPacket packet =
                existed? ClientboundUpdateBlueprintPacket.edit(fileName, newFloorLevel, updatedStructure) :
                        new ClientboundAddBlueprintPacket(group, fileName, fileName,  newFloorLevel, "custom", updatedStructure);
        scheduledEdits.add(packet);
    }

    public void remove(String name) {
        blockDataManager.remove(name);
        final var remove = ClientboundUpdateBlueprintPacket.remove(name);
        scheduledEdits.add(remove);
    }

    public ServerBlueprintBlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public BlueprintTask createTask(UUID taskId, String structureFile, BlockPos startPos, BlockRotation rotation, int floorLevel) {
        final String requirementId = this.findRequirementIdByFileName(structureFile)
                .orElse("custom");
        final BlueprintBlockData serverStructureInfo = blockDataManager.getBlockData(structureFile, rotation, floorLevel);
        final Vec3i size = serverStructureInfo.getSize();
        startPos = startPos.down(floorLevel);
        final BlockPos endPos = getEndPos(startPos, size);
        final Map<BlockPos, BlockState> manualLayer = serverStructureInfo.getLayer(BlueprintDataLayer.MANUAL);
        final Map<BlockPos, BlockState> automatic = serverStructureInfo.getLayer(BlueprintDataLayer.AUTOMATIC);
        final Map<BlockPos, BlockState> entityLayer = serverStructureInfo.getLayer(BlueprintDataLayer.ENTITY);
        return new BlueprintTask(taskId, startPos, endPos, manualLayer, automatic, entityLayer, floorLevel, requirementId);
    }

    public SimpleSelectionTask createDigTask(UUID uuid, BlockPos startPos, int floorLevel, String structureFile, BlockRotation rotation) {
        final BlueprintBlockData serverStructureInfo = blockDataManager.getBlockData(structureFile, rotation);
        final Vec3i size = serverStructureInfo.getSize();
        startPos = startPos.down(floorLevel);
        final BlockPos endPos = getEndPos(startPos, size);

        return new BlueprintDigTask(uuid, startPos, endPos);
    }

    private static BlockPos getEndPos(BlockPos startPos, Vec3i size) {
        return startPos.add(new Vec3i(size.getX()-1, size.getY()-1, size.getZ()-1));
    }

    public void writeToNbt() {
        blockDataManager.writeBlockDataManager();
    }

    public void readFromNbt(NbtCompound compound) {
        blockDataManager.readBlockDataManager(compound);
    }

    private Optional<String> findRequirementIdByFileName(String fileName){
        return blueprintMetadataReader.getPredefinedBlueprints().values().stream().flatMap(Collection::stream)
                .filter(blueprintMetadata -> blueprintMetadata.getFile().equals(fileName))
                .findFirst()
                .map(BlueprintMetadata::getRequirementId);
    }
}
