package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IPlaceCampfireTask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;

import java.util.UUID;

public interface IServerBlueprintManager extends IServerManager, ITickableManager, IWritableManager {
    BlueprintMetadata get(String blueprintId);

    void update(String blueprintId, String blueprintName, BlueprintGroup group, int newCapacity, NbtCompound updatedStructure, int newFloorLevel);

    void remove(String blueprintId);

    IServerStructureBlockDataManager getBlockDataManager();

    ITask createTask(UUID taskId, String blueprintId, BlockPos startPos, BlockRotation rotation);

    ITask createDigTask(UUID taskId, BlockPos startPos, int floorLevel, String blueprintId, BlockRotation rotation);

    IPlaceCampfireTask createInstantPlaceTask(String blueprintId, BlockPos start, BlockRotation rotation);

    void read(NbtCompound nbt);
}
