package org.minefortress.blueprints.manager;

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
import org.minefortress.network.ClientboundEditBlueprintPacket;
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
                            new BlueprintMetadata("Small House 1", "house")
                    )
            ),
            entry(
                    BlueprintGroup.DECORATION,
                    Collections.emptyList()
            )
    );

    private boolean initialized = false;

    private final ServerBlueprintBlockDataManager blockDataManager;

    public ServerBlueprintManager(MinecraftServer server) {
        this.blockDataManager = new ServerBlueprintBlockDataManager(server);
    }

    public void tick(ServerPlayerEntity player) {
        if(!initialized) {
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
    }

    public void update(String fileName, ServerPlayerEntity player, NbtCompound updatedStructure) {
        blockDataManager.update(fileName, updatedStructure);
        final ClientboundEditBlueprintPacket packet = new ClientboundEditBlueprintPacket(fileName, updatedStructure);
        FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_EDIT_BLUEPRINT, packet);
    }

    public ServerBlueprintBlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public BlueprintTask createTask(UUID taskId, String structureFile, BlockPos startPos, BlockRotation rotation) {
        final BlueprintBlockData serverStructureInfo = blockDataManager.getBlockData(structureFile, rotation);
        final Vec3i size = serverStructureInfo.getSize();
        final BlockPos endPos = startPos.add(new Vec3i(size.getX(), size.getY(), size.getZ()));
        return new BlueprintTask(taskId, startPos, endPos, serverStructureInfo.getLayer(BlueprintDataLayer.MANUAL), Collections.emptyMap(), Collections.emptyMap());
    }
}
