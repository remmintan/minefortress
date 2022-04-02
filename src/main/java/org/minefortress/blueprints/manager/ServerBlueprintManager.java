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
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;
import org.minefortress.tasks.BlueprintDigTask;
import org.minefortress.tasks.BlueprintTask;
import org.minefortress.tasks.SimpleSelectionTask;

import java.util.*;

import static java.util.Map.entry;

public class ServerBlueprintManager {

    private static final Map<BlueprintGroup, List<BlueprintMetadata>> PREDEFINED_BLUEPRINTS = Map.ofEntries(
            entry(
                    BlueprintGroup.LIVING_HOUSES,
                    Arrays.asList(
                            //wooden_miner_house.nbt
                            new BlueprintMetadata("Small House 1", "small_house_1").setRequirementId("house"),
                            //small_house_2.nbt
                            new BlueprintMetadata("Small House 2", "small_house_2").setRequirementId("house"),
                            //small_house_3.nbt
                            new BlueprintMetadata("Small House 3", "small_house_3").setRequirementId("house"),
                            //small_house_4.nbt
                            new BlueprintMetadata("Small House 4", "small_house_4").setRequirementId("house"),
                            //small_house_5.nbt
                            new BlueprintMetadata("Small House 5", "small_house_5", 1).setRequirementId("house"),
                            //small_house_6.nbt
                            new BlueprintMetadata("Small House 6", "small_house_6").setRequirementId("house"),
                            //small_house_7.nbt
                            new BlueprintMetadata("Small House 7", "small_house_7").setRequirementId("house"),
                            //small_house_8.nbt
                            new BlueprintMetadata("Small House 8", "small_house_8", 2).setRequirementId("house"),
                            //medium_house_1.nbt
                            new BlueprintMetadata("Medium House 1", "medium_house_1", 1, true).setRequirementId("house"),
                            //medium_house_2.nbt
                            new BlueprintMetadata("Medium House 2", "medium_house_2", true).setRequirementId("house"),
                            //big_house_1.nbt
                            new BlueprintMetadata("Big House 1", "big_house_1", 1, true).setRequirementId("house")
                    )
            ),
            entry(
                    BlueprintGroup.WORKSHOPS,
                    Arrays.asList(
                            new BlueprintMetadata("Wooden Miner's House", "wooden_miner_house").setRequirementId("miner_wooden"),
                            //armorer_house_1.nbt
                            new BlueprintMetadata("Armorer House 1", "armorer_house_1").setRequirementId("armorer"),
                            //butcher_shop_1.nbt
                            new BlueprintMetadata("Butcher Shop 1", "butcher_shop_1").setRequirementId("butcher"),
                            //butcher_shop_2.nbt
                            new BlueprintMetadata("Butcher Shop 2", "butcher_shop_2", 1).setRequirementId("butcher"),
                            //cartographer_1.nbt
                            new BlueprintMetadata("Cartographer 1", "cartographer_1", 1).setRequirementId("cartographer"),
                            //fisher_cottage_1.nbt
                            new BlueprintMetadata("Fisher Cottage 1", "fisher_cottage_1", 2).setRequirementId("fisher"),
                            //fletcher_house_1.nbt
                            new BlueprintMetadata("Fletcher House 1", "fletcher_house_1", 1).setRequirementId("fletcher"),
                            //masons_house_1.nbt
                            new BlueprintMetadata("Masons House 1", "masons_house_1").setRequirementId("masons"),
                            //shepherds_house_1.nbt
                            new BlueprintMetadata("Shepherds House 1", "shepherds_house_1", 1).setRequirementId("shepherd"),
                            //tannery_1.nbt
                            new BlueprintMetadata("Tannery 1", "tannery_1").setRequirementId("tanner"),
                            //tool_smith_1.nbt
                            new BlueprintMetadata("Tool Smith 1", "tool_smith_1").setRequirementId("crafter"),
                            //weaponsmith_1.nbt
                            new BlueprintMetadata("Weaponsmith 1", "weaponsmith_1").setRequirementId("blacksmith")
                    )
            ),
            entry(
                    BlueprintGroup.SOCIAL_BUOLDINGS,
                    Arrays.asList(
                            //library_1.nbt
                            new BlueprintMetadata("Library 1", "library_1").setRequirementId("social"),
                            //library_2.nbt
                            new BlueprintMetadata("Library 2", "library_2", 1, true).setRequirementId("social"),
                            //temple_3.nbt
                            new BlueprintMetadata("Temple 3", "temple_3", true).setRequirementId("social"),
                            //temple_4.nbt
                            new BlueprintMetadata("Temple 4", "temple_4", true).setRequirementId("social")
                    )
            ),
            entry(
                    BlueprintGroup.FARMS,
                    Arrays.asList(
                            //animal_pen_1.nbt
                            new BlueprintMetadata("Animal Pen 1", "animal_pen_1", 1).setRequirementId("shepherd"),
                            //animal_pen_2.nbt
                            new BlueprintMetadata("Animal Pen 2", "animal_pen_2", 1, true).setRequirementId("shepherd"),
                            //animal_pen_3.nbt
                            new BlueprintMetadata("Animal Pen 3", "animal_pen_3", 1, true).setRequirementId("shepherd"),
                            //small_farm_1.nbt
                            new BlueprintMetadata("Small Farm 1", "small_farm_1", true).setRequirementId("farmer"),
                            //large_farm_1.nbt
                            new BlueprintMetadata("Large Farm 1", "large_farm_1", true).setRequirementId("farmer"),
                            //stable_1.nbt
                            new BlueprintMetadata("Stable 1", "stable_1", 1, true).setRequirementId("stableman"),
                            //stable_2.nbt
                            new BlueprintMetadata("Stable 2", "stable_2", 1, true).setRequirementId("stableman")
                    )
            ),
            entry(
                    BlueprintGroup.DECORATION,
                    Arrays.asList(
                            //accessory_1.nbt
                            new BlueprintMetadata("Accessory 1", "accessory_1").setRequirementId("decor"),
                            //fountain_01.nbt
                            new BlueprintMetadata("Fountain 01", "fountain_01", 1, true).setRequirementId("decor"),
                            //meeting_point_1.nbt
                            new BlueprintMetadata("Meeting Point 1", "meeting_point_1", 1, true).setRequirementId("decor"),
                            //meeting_point_2.nbt
                            new BlueprintMetadata("Meeting Point 2", "meeting_point_2", 1, true).setRequirementId("decor"),
                            //meeting_point_3.nbt
                            new BlueprintMetadata("Meeting Point 3", "meeting_point_3", 1, true).setRequirementId("decor"),
                            //meeting_point_4.nbt
                            new BlueprintMetadata("Meeting Point 4", "meeting_point_4", 1, true).setRequirementId("decor"),
                            //meeting_point_5.nbt
                            new BlueprintMetadata("Meeting Point 5", "meeting_point_5", 1, true).setRequirementId("decor")
                    )
            )
    );

    private boolean initialized = false;

    private final Map<String, Integer> updatedFloorLevel = new HashMap<>();

    private final ServerBlueprintBlockDataManager blockDataManager;
    private final Queue<ClientboundUpdateBlueprintPacket> scheduledEdits = new ArrayDeque<>();

    public ServerBlueprintManager(MinecraftServer server) {
        this.blockDataManager = new ServerBlueprintBlockDataManager(server);
    }

    public void tick(ServerPlayerEntity player) {
        if(!initialized) {
            scheduledEdits.clear();
            final ClientboundResetBlueprintPacket resetpacket = new ClientboundResetBlueprintPacket();
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_RESET_BLUEPRINT, resetpacket);

            for(Map.Entry<BlueprintGroup, List<BlueprintMetadata>> entry : PREDEFINED_BLUEPRINTS.entrySet()) {
                for(BlueprintMetadata blueprintMetadata : entry.getValue()) {
                    final String file = blueprintMetadata.getFile();
                    final int floorLevel = updatedFloorLevel.containsKey(file)?updatedFloorLevel.get(file):blueprintMetadata.getFloorLevel();
                    final NbtCompound structureNbt = blockDataManager.getStructureNbt(file);
                    final ClientboundAddBlueprintPacket packet = new ClientboundAddBlueprintPacket(entry.getKey(), blueprintMetadata.getName(), file, structureNbt, floorLevel, blueprintMetadata.isPremium());
                    FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_ADD_BLUEPRINT, packet);
                }
            }

            initialized = true;
        }

        if(!scheduledEdits.isEmpty()) {
            final ClientboundUpdateBlueprintPacket packet = scheduledEdits.remove();
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_UPDATE_BLUEPRINT, packet);
        }
    }

    public void update(String fileName, NbtCompound updatedStructure, int newFloorLevel) {
        blockDataManager.update(fileName, updatedStructure);
        updatedFloorLevel.put(fileName, newFloorLevel);
        final ClientboundUpdateBlueprintPacket packet = new ClientboundUpdateBlueprintPacket(fileName, newFloorLevel, updatedStructure);
        scheduledEdits.add(packet);
    }

    public ServerBlueprintBlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public BlueprintTask createTask(UUID taskId, String structureFile, BlockPos startPos, BlockRotation rotation, int floorLevel) {
        final String requirementId = this.findRequirementIdByFileName(structureFile)
                .orElseThrow(() -> new IllegalArgumentException("Structure file not found: " + structureFile));
        final BlueprintBlockData serverStructureInfo = blockDataManager.getBlockData(structureFile, rotation, floorLevel);
        final Vec3i size = serverStructureInfo.getSize();
        startPos = startPos.down(floorLevel);
        final BlockPos endPos = startPos.add(new Vec3i(size.getX(), size.getY(), size.getZ()));
        final Map<BlockPos, BlockState> manualLayer = serverStructureInfo.getLayer(BlueprintDataLayer.MANUAL);
        final Map<BlockPos, BlockState> automatic = serverStructureInfo.getLayer(BlueprintDataLayer.AUTOMATIC);
        final Map<BlockPos, BlockState> entityLayer = serverStructureInfo.getLayer(BlueprintDataLayer.ENTITY);
        return new BlueprintTask(taskId, startPos, endPos, manualLayer, automatic, entityLayer, floorLevel, requirementId);
    }

    public SimpleSelectionTask createDigTask(UUID uuid, BlockPos startPos, int floorLevel, String structureFile, BlockRotation rotation) {
        final BlueprintBlockData serverStructureInfo = blockDataManager.getBlockData(structureFile, rotation);
        final Vec3i size = serverStructureInfo.getSize();
        startPos = startPos.down(floorLevel);
        final BlockPos endPos = startPos.add(new Vec3i(size.getX(), size.getY(), size.getZ()));

        return new BlueprintDigTask(uuid, startPos, endPos);
    }

    public void writeToNbt(NbtCompound compound) {
        blockDataManager.writeBlockDataManager(compound);

        final NbtCompound floorLevel = new NbtCompound();
        for(Map.Entry<String, Integer> entry : updatedFloorLevel.entrySet()) {
            floorLevel.putInt(entry.getKey(), entry.getValue());
        }
        compound.put("floorLevel", floorLevel);
    }

    public void readFromNbt(NbtCompound compound) {
        blockDataManager.readBlockDataManager(compound);

        if(compound.contains("floorLevel")) {
            final NbtCompound floorLevel = compound.getCompound("floorLevel");
            for(String key : floorLevel.getKeys()) {
                updatedFloorLevel.put(key, floorLevel.getInt(key));
            }
        }
    }

    private Optional<String> findRequirementIdByFileName(String fileName){
        return PREDEFINED_BLUEPRINTS.values().stream().flatMap(Collection::stream)
                .filter(blueprintMetadata -> blueprintMetadata.getFile().equals(fileName))
                .findFirst()
                .map(BlueprintMetadata::getRequirementId);
    }
}
