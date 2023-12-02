package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.ScreenType;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.entities.IPawnNameGenerator;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IServerFortressManager {
    void setBorderVisibilityState(boolean borderEnabled);
    void setCampfireVisibilityState(boolean campfireEnabled);
    void scheduleSync();
    void syncOnJoin(boolean campfireEnabled, boolean borderEnabled);
    IPawnNameGenerator getNameGenerator();
    void replaceColonistWithTypedPawn(LivingEntity colonist, String warriorId, EntityType<? extends LivingEntity> entityType);
    Optional<IAutomationArea> getAutomationAreaByRequirementId(String requirement, ServerPlayerEntity masterPlayer);
    List<BlockPos> getSpecialBlocksByType(Block block, boolean blueprint);
    double getCampfireWarmRadius();
    boolean isPositionWithinFortress(BlockPos pos);
    void addSpecialBlocks(Block block, BlockPos blockPos, boolean blueprint);
    boolean isBlockSpecial(Block block);

    void jumpToCampfire(ServerPlayerEntity player);

    void repairBuilding(ServerPlayerEntity player, UUID taskId, UUID buildingId);

    boolean isSurvival();

    boolean isCreative();
    Optional<BlockPos> getRandomPosWithinFortress();

    List<IWorkerPawn> getFreeColonists();
    List<ITargetedPawn> getAllTargetedPawns();

    void increaseMaxColonistsCount();

    void decreaseMaxColonistsCount();

    void setupCenter(BlockPos pos, World world, ServerPlayerEntity player);

    void openHandledScreen(ScreenType type, ServerPlayerEntity player, BlockPos pos);

    void setGamemode(FortressGamemode fortressGamemode);
    void expandTheVillage(BlockPos pos);
    void addColonist(LivingEntity pawn);
    BlockPos getFortressCenter();
    Optional<BlockPos> getRandomPositionAroundCampfire();
    void setSpawnPawns(boolean spawnPawns);

}
