package org.minefortress.blueprints.manager;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.blueprints.data.StrctureBlockData;
import org.minefortress.blueprints.data.BlueprintDataLayer;
import org.minefortress.blueprints.data.ServerStructureBlockDataManager;
import org.minefortress.network.s2c.ClientboundAddBlueprintPacket;
import org.minefortress.network.s2c.ClientboundResetBlueprintPacket;
import org.minefortress.network.s2c.ClientboundUpdateBlueprintPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;
import org.minefortress.tasks.BlueprintDigTask;
import org.minefortress.tasks.BlueprintTask;
import org.minefortress.tasks.SimpleSelectionTask;
import Z;
import java.util.*;
import java.util.function.Supplier;

public class ServerBlueprintManager {

    private boolean initialized = false;

    private final ServerStructureBlockDataManager blockDataManager;
    private final BlueprintMetadataReader blueprintMetadataReader;
    private final Queue<FortressS2CPacket> scheduledEdits = new ArrayDeque<>();

    public ServerBlueprintManager(MinecraftServer server, Supplier<UUID> userIdProvider) {
        this.blueprintMetadataReader = new BlueprintMetadataReader(server);
        this.blockDataManager = new ServerStructureBlockDataManager(server, blueprintMetadataReader::convertFilenameToGroup, userIdProvider);
    }

    public void tick(ServerPlayerEntity player) {
        if(!initialized) {
            blueprintMetadataReader.read();
            scheduledEdits.clear();
            final ClientboundResetBlueprintPacket resetpacket = new ClientboundResetBlueprintPacket();
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_RESET_BLUEPRINT, resetpacket);

            for(Map.Entry<BlueprintGroup, List<BlueprintMetadata>> entry : blueprintMetadataReader.getPredefinedBlueprints().entrySet()) {
                for(BlueprintMetadata blueprintMetadata : entry.getValue()) {
                    final String file = blueprintMetadata.getId();
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
            final FortressS2CPacket packet = scheduledEdits.remove();
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
        final FortressS2CPacket packet =
                existed? ClientboundUpdateBlueprintPacket.edit(fileName, newFloorLevel, updatedStructure) :
                        new ClientboundAddBlueprintPacket(group, fileName, fileName,  newFloorLevel, "custom", updatedStructure);
        scheduledEdits.add(packet);
    }

    public void remove(String name) {
        blockDataManager.remove(name);
        final var remove = ClientboundUpdateBlueprintPacket.remove(name);
        scheduledEdits.add(remove);
    }

    public ServerStructureBlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public BlueprintTask createTask(UUID taskId, String blueprintId, BlockPos startPos, BlockRotation rotation, int floorLevel) {
        final String requirementId = this.findRequirementById(blueprintId)
                .orElse("custom");
        final StrctureBlockData serverStructureInfo = blockDataManager.getBlockData(blueprintId, rotation, floorLevel);
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
                requirementId,
                blueprintId
        );
    }

    public SimpleSelectionTask createDigTask(UUID uuid, BlockPos startPos, int floorLevel, String structureFile, BlockRotation rotation) {
        final StrctureBlockData serverStructureInfo = blockDataManager.getBlockData(structureFile, rotation);
        final Vec3i size = serverStructureInfo.getSize();
        startPos = startPos.down(floorLevel);
        final BlockPos endPos = getEndPos(startPos, size);

        return new BlueprintDigTask(uuid, startPos, endPos);
    }

    private static BlockPos getEndPos(BlockPos startPos, Vec3i size) {
        return startPos.add(new Vec3i(size.getX()-1, size.getY()-1, size.getZ()-1));
    }

    public void write() {
        blockDataManager.writeBlockDataManager();
    }

    public void read() {
        read(null);
        initialized = false;
    }

    public void read(NbtCompound compound) {
        blockDataManager.readBlockDataManager(compound);
    }

    private Optional<String> findRequirementById(String blueprintId){
        return blueprintMetadataReader.getPredefinedBlueprints()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(blueprintMetadata -> blueprintMetadata.getId().equals(blueprintId))
                .findFirst()
                .map(BlueprintMetadata::getRequirementId);
    }
}
