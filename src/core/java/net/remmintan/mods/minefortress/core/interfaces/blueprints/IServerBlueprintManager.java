package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;

import java.util.UUID;

public interface IServerBlueprintManager {
    void tick(ServerPlayerEntity player);

    void update(String blueprintId, NbtCompound updatedStructure, int newFloorLevel, int capacity, BlueprintGroup group);

    void remove(String blueprintId);

    IServerStructureBlockDataManager getBlockDataManager();

    ITask createTask(UUID taskId, String blueprintId, BlockPos startPos, BlockRotation rotation, int floorLevel);

    ITask createDigTask(UUID taskId, BlockPos startPos, int floorLevel, String blueprintId, BlockRotation rotation);

    void write();

    void read();
}
