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
import org.minefortress.network.ClientboundUpdateBlueprintPacket;
import org.minefortress.network.ClientboundResetBlueprintPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;
import org.minefortress.tasks.BlueprintTask;

import java.util.*;

import static java.util.Map.entry;

public class ServerBlueprintManager {

    private static final Map<BlueprintGroup, List<BlueprintMetadata>> PREDEFINED_BLUEPRINTS = Map.ofEntries(
            entry(
                    BlueprintGroup.LIVING_HOUSES,
                    Arrays.asList(
                            //small_house_1.nbt
                            new BlueprintMetadata("Small House 1", "small_house_1"),
                            //small_house_2.nbt
                            new BlueprintMetadata("Small House 2", "small_house_2"),
                            //small_house_3.nbt
                            new BlueprintMetadata("Small House 3", "small_house_3"),
                            //small_house_4.nbt
                            new BlueprintMetadata("Small House 4", "small_house_4"),
                            //small_house_5.nbt
                            new BlueprintMetadata("Small House 5", "small_house_5"),
                            //small_house_6.nbt
                            new BlueprintMetadata("Small House 6", "small_house_6"),
                            //small_house_7.nbt
                            new BlueprintMetadata("Small House 7", "small_house_7"),
                            //small_house_8.nbt
                            new BlueprintMetadata("Small House 8", "small_house_8"),
                            //medium_house_1.nbt
                            new BlueprintMetadata("Medium House 1", "medium_house_1"),
                            //medium_house_2.nbt
                            new BlueprintMetadata("Medium House 2", "medium_house_2"),
                            //big_house_1.nbt
                            new BlueprintMetadata("Big House 1", "big_house_1")
                    )
            ),
            entry(
                    BlueprintGroup.WORKSHOPS,
                    Arrays.asList(
                            //armorer_house_1.nbt
                            new BlueprintMetadata("Armorer House 1", "armorer_house_1"),
                            //butcher_shop_1.nbt
                            new BlueprintMetadata("Butcher Shop 1", "butcher_shop_1"),
                            //butcher_shop_2.nbt
                            new BlueprintMetadata("Butcher Shop 2", "butcher_shop_2"),
                            //cartographer_1.nbt
                            new BlueprintMetadata("Cartographer 1", "cartographer_1"),
                            //fisher_cottage_1.nbt
                            new BlueprintMetadata("Fisher Cottage 1", "fisher_cottage_1"),
                            //fletcher_house_1.nbt
                            new BlueprintMetadata("Fletcher House 1", "fletcher_house_1"),
                            //masons_house_1.nbt
                            new BlueprintMetadata("Masons House 1", "masons_house_1"),
                            //shepherds_house_1.nbt
                            new BlueprintMetadata("Shepherds House 1", "shepherds_house_1"),
                            //tannery_1.nbt
                            new BlueprintMetadata("Tannery 1", "tannery_1"),
                            //tool_smith_1.nbt
                            new BlueprintMetadata("Tool Smith 1", "tool_smith_1"),
                            //weaponsmith_1.nbt
                            new BlueprintMetadata("Weaponsmith 1", "weaponsmith_1")
                    )
            ),
            entry(
                    BlueprintGroup.SOCIAL_BUOLDINGS,
                    Arrays.asList(
                            //library_1.nbt
                            new BlueprintMetadata("Library 1", "library_1"),
                            //library_2.nbt
                            new BlueprintMetadata("Library 2", "library_2"),
                            //temple_3.nbt
                            new BlueprintMetadata("Temple 3", "temple_3"),
                            //temple_4.nbt
                            new BlueprintMetadata("Temple 4", "temple_4")
                    )
            ),
            entry(
                    BlueprintGroup.FARMS,
                    Arrays.asList(
                            //animal_pen_1.nbt
                            new BlueprintMetadata("Animal Pen 1", "animal_pen_1"),
                            //animal_pen_2.nbt
                            new BlueprintMetadata("Animal Pen 2", "animal_pen_2"),
                            //animal_pen_3.nbt
                            new BlueprintMetadata("Animal Pen 3", "animal_pen_3"),
                            //small_farm_1.nbt
                            new BlueprintMetadata("Small Farm 1", "small_farm_1"),
                            //large_farm_1.nbt
                            new BlueprintMetadata("Large Farm 1", "large_farm_1"),
                            //stable_1.nbt
                            new BlueprintMetadata("Stable 1", "stable_1"),
                            //stable_2.nbt
                            new BlueprintMetadata("Stable 2", "stable_2")
                    )
            ),
            entry(
                    BlueprintGroup.DECORATION,
                    Arrays.asList(
                            //accessory_1.nbt
                            new BlueprintMetadata("Accessory 1", "accessory_1"),
                            //fountain_01.nbt
                            new BlueprintMetadata("Fountain 01", "fountain_01"),
                            //meeting_point_1.nbt
                            new BlueprintMetadata("Meeting Point 1", "meeting_point_1"),
                            //meeting_point_2.nbt
                            new BlueprintMetadata("Meeting Point 2", "meeting_point_2"),
                            //meeting_point_3.nbt
                            new BlueprintMetadata("Meeting Point 3", "meeting_point_3"),
                            //meeting_point_4.nbt
                            new BlueprintMetadata("Meeting Point 4", "meeting_point_4"),
                            //meeting_point_5.nbt
                            new BlueprintMetadata("Meeting Point 5", "meeting_point_5")
                    )
            )
    );

    private boolean initialized = false;

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
                    final NbtCompound structureNbt = blockDataManager.getStructureNbt(blueprintMetadata.getFile());
                    final ClientboundAddBlueprintPacket packet = new ClientboundAddBlueprintPacket(entry.getKey(), blueprintMetadata.getName(), blueprintMetadata.getFile(), structureNbt);
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

    public void update(String fileName, NbtCompound updatedStructure) {
        blockDataManager.update(fileName, updatedStructure);
        final ClientboundUpdateBlueprintPacket packet = new ClientboundUpdateBlueprintPacket(fileName, updatedStructure);
        scheduledEdits.add(packet);
    }

    public ServerBlueprintBlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public BlueprintTask createTask(UUID taskId, String structureFile, BlockPos startPos, BlockRotation rotation) {
        final BlueprintBlockData serverStructureInfo = blockDataManager.getBlockData(structureFile, rotation);
        final Vec3i size = serverStructureInfo.getSize();
        final BlockPos endPos = startPos.add(new Vec3i(size.getX(), size.getY(), size.getZ()));
        final Map<BlockPos, BlockState> manualLayer = serverStructureInfo.getLayer(BlueprintDataLayer.MANUAL);
        final Map<BlockPos, BlockState> automatic = serverStructureInfo.getLayer(BlueprintDataLayer.AUTOMATIC);
        final Map<BlockPos, BlockState> entityLayer = serverStructureInfo.getLayer(BlueprintDataLayer.ENTITY);
        return new BlueprintTask(taskId, startPos, endPos, manualLayer, automatic, entityLayer);
    }

    public void writeToNbt(NbtCompound compound) {
        blockDataManager.writeBlockDataManager(compound);
    }

    public void readFromNbt(NbtCompound compound) {
        blockDataManager.readBlockDataManager(compound);
    }
}
