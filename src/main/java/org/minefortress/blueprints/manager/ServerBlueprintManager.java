package org.minefortress.blueprints.manager;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.*;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundAddBlueprintPacket;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundResetBlueprintPacket;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundUpdateBlueprintPacket;
import org.minefortress.MineFortressMod;
import org.minefortress.blueprints.data.ServerStructureBlockDataManager;
import org.minefortress.blueprints.world.FortressServerWorld;
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
        this.blockDataManager = new ServerStructureBlockDataManager(server, blueprintMetadataReader::convertFilenameToGroup, userIdProvider);
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

    @Override
    public void update(String fileName, NbtCompound updatedStructure, int newFloorLevel, BlueprintGroup group) {
        final var existed = blockDataManager.update(fileName, updatedStructure, newFloorLevel, group);
        final FortressS2CPacket packet =
                existed? ClientboundUpdateBlueprintPacket.edit(fileName, newFloorLevel, updatedStructure) :
                        new ClientboundAddBlueprintPacket(group, fileName, fileName,  newFloorLevel, "custom", updatedStructure);
        scheduledEdits.add(packet);
    }

    @Override
    public void remove(String name) {
        blockDataManager.remove(name);
        final var remove = ClientboundUpdateBlueprintPacket.remove(name);
        scheduledEdits.add(remove);
    }

    @Override
    public IServerStructureBlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    @Override
    public BlueprintTask createTask(UUID taskId, String blueprintId, BlockPos startPos, BlockRotation rotation, int floorLevel) {
        final String requirementId = this.findRequirementById(blueprintId)
                .orElse("custom");
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
                requirementId,
                blueprintId
        );
    }

    @Override
    public SimpleSelectionTask createDigTask(UUID uuid, BlockPos startPos, int floorLevel, String structureFile, BlockRotation rotation) {
        final IStructureBlockData serverStructureInfo = blockDataManager.getBlockData(structureFile, rotation);
        final Vec3i size = serverStructureInfo.getSize();
        startPos = startPos.down(floorLevel);
        final BlockPos endPos = getEndPos(startPos, size);

        return new BlueprintDigTask(uuid, startPos, endPos);
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
        read(null);
        initialized = false;
    }

    @Override
    public void read(NbtCompound compound) {
        blockDataManager.readBlockDataManager(compound);
    }

    @Override
    public void finishBlueprintEdit(boolean shouldSave, MinecraftServer server, ServerPlayerEntity player) {
        final FortressServerWorld fortressServerWorld = (FortressServerWorld) player.getWorld();

        final String fileName = fortressServerWorld.getFileName();

        final Identifier updatedStructureIdentifier = new Identifier(MineFortressMod.MOD_ID, fileName.replaceAll("[^a-z0-9/._-]", "_"));
        final StructureTemplateManager structureManager = server.getStructureTemplateManager();
        final StructureTemplate structureToUpdate = structureManager.getTemplateOrBlank(updatedStructureIdentifier);
        fortressServerWorld.enableSaveStructureMode();

        final BlockPos start = new BlockPos(0, 1, 0);
        final BlockPos end = new BlockPos(15, 32, 15);
        final Iterable<BlockPos> allPositions = BlockPos.iterate(start, end);

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for(BlockPos pos : allPositions) {
            final BlockState blockState = fortressServerWorld.getBlockState(pos);
            final int y = pos.getY();
            if(isStateWasChanged(blockState, y)) {
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());

                maxX = Math.max(maxX, pos.getX());
                maxY = Math.max(maxY, pos.getY());
                maxZ = Math.max(maxZ, pos.getZ());
            }
        }

        final BlockPos min = new BlockPos(minX, minY, minZ);
        final BlockPos max = new BlockPos(maxX, maxY, maxZ);
        final BlockPos dimensions = max.subtract(min).add(1, 1, 1);

        structureToUpdate.saveFromWorld(fortressServerWorld, min, dimensions, true, Blocks.VOID_AIR);
        fortressServerWorld.disableSaveStructureMode();

        final int newFloorLevel = 16 - min.getY();

        final NbtCompound updatedStructure = new NbtCompound();
        structureToUpdate.writeNbt(updatedStructure);
        this.update(fileName, updatedStructure, newFloorLevel, fortressServerWorld.getBlueprintGroup());

    }

    private boolean isStateWasChanged(BlockState blockState, int y) {
        if(blockState.isOf(Blocks.VOID_AIR)) return false;
        if(y > 15) return !blockState.isAir();
        if(y == 15) return !blockState.isOf(Blocks.GRASS_BLOCK);
        return !blockState.isOf(Blocks.DIRT);
    }

    private Optional<String> findRequirementById(String blueprintId){
        return blueprintMetadataReader.getPredefinedBlueprints()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(blueprintMetadata -> blueprintMetadata.getId().equals(blueprintId))
                .findFirst()
                .map(IBlueprintMetadata::getRequirementId);
    }
}
