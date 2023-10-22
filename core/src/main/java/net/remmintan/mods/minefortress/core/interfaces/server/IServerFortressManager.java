package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.ScreenType;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IServerFortressManager {

    void jumpToCampfire(ServerPlayerEntity player);

    void repairBuilding(ServerPlayerEntity player, UUID taskId, UUID buildingId);

    boolean isSurvival();

    boolean isCreative();
    Optional<BlockPos> getRandomPosWithinFortress();

    List<IWorkerPawn> getFreeColonists();

    void increaseMaxColonistsCount();

    void decreaseMaxColonistsCount();

    void setupCenter(BlockPos pos, World world, ServerPlayerEntity player);

    void openHandledScreen(ScreenType type, ServerPlayerEntity player, BlockPos pos);

    void setGamemode(FortressGamemode fortressGamemode);

    void addColonist(LivingEntity pawn);

    IServerResourceManager getResourceManager();

    BlockPos getFortressCenter();
    Optional<BlockPos> getRandomPositionAroundCampfire();

}
