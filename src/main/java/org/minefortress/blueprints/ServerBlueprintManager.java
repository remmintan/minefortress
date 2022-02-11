package org.minefortress.blueprints;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.tasks.BlueprintTask;

import java.util.UUID;

public class ServerBlueprintManager {

    private final BlueprintBlockDataManager blockDataManager;


    public ServerBlueprintManager(MinecraftServer server) {
        this.blockDataManager = ((FortressServer) server).getBlueprintBlockDataManager();
    }

    public BlueprintTask createTask(UUID taskId, String structureFile, BlockPos startPos, BlockRotation rotation) {
        final BlueprintBlockDataManager.BlueprintBlockData serverStructureInfo = blockDataManager.getBlockData(structureFile, rotation, true);
        final Vec3i size = serverStructureInfo.getSize();
        final BlockPos endPos = startPos.add(new Vec3i(size.getX(), size.getY(), size.getZ()));
        return new BlueprintTask(taskId, startPos, endPos, serverStructureInfo.getManualLayer(), serverStructureInfo.getEntityLayer(), serverStructureInfo.getAutomaticLayer());
    }

}
