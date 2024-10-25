package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;

import java.util.UUID;

public interface IServerBlueprintManager {
    void tick(ServerPlayerEntity player);

    void update(String fileName, NbtCompound updatedStructure, int newFloorLevel, int capacity, BlueprintGroup group);

    void remove(String name);

    IServerStructureBlockDataManager getBlockDataManager();

    ITask createTask(UUID taskId, String blueprintId, BlockPos startPos, BlockRotation rotation, int floorLevel);

    ITask createDigTask(UUID uuid, BlockPos startPos, int floorLevel, String structureFile, BlockRotation rotation);

    void write();

    void read();

    void read(NbtCompound compound);

    void finishBlueprintEdit(boolean shouldSave, MinecraftServer server, ServerPlayerEntity player);
}
