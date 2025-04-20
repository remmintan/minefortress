package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.ScreenType;
import net.remmintan.mods.minefortress.core.dtos.PawnSkin;
import net.remmintan.mods.minefortress.core.interfaces.IFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.entities.IPawnNameGenerator;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IProfessional;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IServerFortressManager extends IFortressManager, IWritableManager, ISyncableServerManager {
    void scheduleSync();

    void killAllPawns();

    Set<IProfessional> getProfessionals();

    BlockPos getFortressCenter();

    IPawnNameGenerator getNameGenerator();
    void replaceColonistWithTypedPawn(LivingEntity colonist, String warriorId, EntityType<? extends LivingEntity> entityType);

    Optional<IAutomationArea> getAutomationAreaByProfessionType(ProfessionType professionType);
    double getCampfireWarmRadius();
    boolean isPositionWithinFortress(BlockPos pos);

    void jumpToCampfire(ServerPlayerEntity player);

    void teleportToCampfireGround(ServerPlayerEntity player);

    void repairBuilding(ServerPlayerEntity player, BlockPos pos, List<Integer> selectedPawns);

    List<ITargetedPawn> getAllTargetedPawns();

    void increaseMaxColonistsCount();
    void decreaseMaxColonistsCount();

    void spawnInitialPawns();

    void openHandledScreen(ScreenType type, ServerPlayerEntity player, BlockPos pos);

    void expandTheVillage(BlockPos pos);

    void addPawn(LivingEntity pawn);
    Optional<BlockPos> getRandomPositionAroundCampfire();

    Optional<LivingEntity> spawnPawnNearCampfire();
    void setSpawnPawns(boolean spawnPawns);
    void spawnDebugEntitiesAroundCampfire(EntityType<? extends IFortressAwareEntity> entityType, int num, ServerPlayerEntity player);

    Optional<IProfessional> getPawnWithoutAProfession();

    List<IWorkerPawn> getFreeWorkers();

    void setPawnsSkin(PawnSkin skin);

    boolean isPawnsSkinSet();

    void tick(@Nullable final ServerPlayerEntity fortressOwner);

}
