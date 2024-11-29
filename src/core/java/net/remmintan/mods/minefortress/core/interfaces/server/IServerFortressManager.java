package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.ScreenType;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.entities.IPawnNameGenerator;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.ITargetedPawn;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IServerFortressManager {
    void scheduleSync();

    void syncOnJoin();
    IPawnNameGenerator getNameGenerator();
    void replaceColonistWithTypedPawn(LivingEntity colonist, String warriorId, EntityType<? extends LivingEntity> entityType);

    Optional<IAutomationArea> getAutomationAreaByProfessionType(ProfessionType professionType, ServerPlayerEntity masterPlayer);
    List<BlockPos> getSpecialBlocksByType(Block block, boolean blueprint);
    double getCampfireWarmRadius();
    boolean isPositionWithinFortress(BlockPos pos);
    void addSpecialBlocks(Block block, BlockPos blockPos, boolean blueprint);
    boolean isBlockSpecial(Block block);

    void jumpToCampfire(ServerPlayerEntity player);

    void repairBuilding(ServerPlayerEntity player, UUID taskId, BlockPos pos, List<Integer> selectedPawns);

    boolean isSurvival();

    boolean isCreative();
    List<ITargetedPawn> getAllTargetedPawns();

    void increaseMaxColonistsCount();

    void decreaseMaxColonistsCount();

    void setupCenter(BlockPos pos, ServerPlayerEntity player);

    void openHandledScreen(ScreenType type, ServerPlayerEntity player, BlockPos pos);

    void setGamemode(FortressGamemode fortressGamemode);
    void expandTheVillage(BlockPos pos);
    void addColonist(LivingEntity pawn);
    BlockPos getFortressCenter();
    Optional<BlockPos> getRandomPositionAroundCampfire();
    void setSpawnPawns(boolean spawnPawns);
    void spawnDebugEntitiesAroundCampfire(EntityType<? extends IFortressAwareEntity> entityType, int num, ServerPlayerEntity player);

}
