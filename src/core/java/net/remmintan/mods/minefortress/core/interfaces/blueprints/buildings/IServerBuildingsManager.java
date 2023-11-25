package net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;

import java.util.Optional;
import java.util.UUID;

public interface IServerBuildingsManager extends IServerManager {
    long getTotalBedsCount();
    void destroyBuilding(UUID id);
    void doRepairConfirmation(UUID id, ServerPlayerEntity player);
    Optional<IFortressBuilding> findNearest(BlockPos pos);
    void addBuilding(IFortressBuilding building);
    Optional<HostileEntity> getRandomBuildingAttacker();
    Optional<BlockPos> getFreeBed();
    NbtCompound toNbt();
    void readFromNbt(NbtCompound buildingsTag);
    void reset();
    boolean isPartOfAnyBuilding(BlockPos pos);
    boolean hasRequiredBuilding(String requirementId, int minCount);
    Optional<IFortressBuilding> getBuildingById(UUID id);

}
