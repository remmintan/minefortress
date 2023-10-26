package net.remmintan.mods.minefortress.core.interfaces.infuence;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlockDataProvider;

import java.util.UUID;

public interface IServerInfluenceManager {
    void tick(ServerPlayerEntity player);
    void addCapturePosition(UUID taskId, BlockPos pos, ServerPlayerEntity player);
    void checkNewPositionAndUpdateClientState(BlockPos pos, ServerPlayerEntity player);
    ICaptureTask getCaptureTask();
    void addInfluencePosition(BlockPos pos);
    void failCaptureTask(ICaptureTask task);
    IBlockDataProvider getBlockDataProvider();

    void addCenterAsInfluencePosition();
    void sync();
    void write(NbtCompound tag);
    void read(NbtCompound tag);

}
