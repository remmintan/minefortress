package net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;

import java.util.Optional;
import java.util.UUID;

public interface IServerBuildingsManager extends IServerManager {
    long getTotalBedsCount();
    void destroyBuilding(UUID id);
    void doRepairConfirmation(UUID id, ServerPlayerEntity player);
    Optional<IFortressBuilding> findNearest(BlockPos pos);

    Optional<IFortressBuilding> findNearest(BlockPos pos, ProfessionType requirement);
    void addBuilding(IFortressBuilding building);
    Optional<HostileEntity> getRandomBuildingAttacker();
    Optional<BlockPos> getFreeBed();
    boolean isPartOfAnyBuilding(BlockPos pos);

    boolean hasRequiredBuilding(ProfessionType type, int level, int minCount);
    Optional<IFortressBuilding> getBuildingById(UUID id);

}
