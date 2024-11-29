package net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings;

import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;

import java.util.Map;
import java.util.Optional;

public interface IServerBuildingsManager extends IServerManager, IWritableManager, ITickableManager {
    void addBuilding(BlueprintMetadata metadata, BlockPos start, BlockPos end, Map<BlockPos, BlockState> mergedBlockData);

    void destroyBuilding(BlockPos pos);

    Optional<IFortressBuilding> getBuilding(BlockPos pos);

    long getTotalBedsCount();

    void doRepairConfirmation(BlockPos pos, ServerPlayerEntity player);
    Optional<IFortressBuilding> findNearest(BlockPos pos);
    Optional<IFortressBuilding> findNearest(BlockPos pos, ProfessionType requirement);

    Optional<HostileEntity> getRandomBuildingAttacker();
    Optional<BlockPos> getFreeBed();
    boolean isPartOfAnyBuilding(BlockPos pos);
    boolean hasRequiredBuilding(ProfessionType type, int level, int minCount);
}
