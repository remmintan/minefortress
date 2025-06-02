package net.remmintan.mods.minefortress.core.interfaces.buildings;

import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IServerBuildingsManager extends IServerManager, IWritableManager, ITickableManager {
    void addBuilding(BlueprintMetadata metadata, BlockPos start, BlockPos end, Map<BlockPos, BlockState> mergedBlockData);

    void destroyBuilding(BlockPos pos);

    Optional<IFortressBuilding> getBuilding(BlockPos pos);

    List<IFortressBuilding> getBuildings(ProfessionType profession);

    List<IFortressBuilding> getBuildings(ProfessionType type, int level);

    long getTotalBedsCount();
    Optional<IFortressBuilding> findNearest(BlockPos pos);
    Optional<IFortressBuilding> findNearest(BlockPos pos, ProfessionType requirement);

    @Nullable BlockPos getRandomPositionToGoTo();

    Optional<HostileEntity> getRandomBuildingAttacker();
    Optional<BlockPos> getFreeBed();
    boolean isPartOfAnyBuilding(BlockPos pos);
    boolean hasRequiredBuilding(ProfessionType type, int level, int minCount);
}
