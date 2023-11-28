package net.remmintan.mods.minefortress.core.interfaces.infuence;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlockDataProvider;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;

import java.util.UUID;

public interface IServerInfluenceManager extends IServerManager {
    void addCapturePosition(UUID taskId, BlockPos pos, ServerPlayerEntity player);
    void checkNewPositionAndUpdateClientState(BlockPos pos, ServerPlayerEntity player);
    ICaptureTask getCaptureTask();
    void addInfluencePosition(BlockPos pos);
    void failCaptureTask(ICaptureTask task);
    IBlockDataProvider getBlockDataProvider();

    void addCenterAsInfluencePosition();
    void sync();

}
