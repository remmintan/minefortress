package net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;

import java.util.Optional;
import java.util.UUID;

public interface IServerBuildingsManager {

    void destroyBuilding(UUID id);
    void doRepairConfirmation(UUID id, ServerPlayerEntity player);
    Optional<IFortressBuilding> findNearest(BlockPos pos);
    void addBuilding(IFortressBuilding building);
    Optional<HostileEntity> getRandomBuildingAttacker();
    Optional<BlockPos> getFreeBed();

}
