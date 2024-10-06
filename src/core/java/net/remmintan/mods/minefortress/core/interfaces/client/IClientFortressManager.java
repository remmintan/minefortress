package net.remmintan.mods.minefortress.core.interfaces.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.dtos.buildings.BuildingHealthRenderInfo;
import net.remmintan.mods.minefortress.core.interfaces.IFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IEssentialBuildingInfo;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireInfo;
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IClientFortressManager extends IFortressManager {

    void jumpToCampfire();

    void updateBuildings(List<IEssentialBuildingInfo> buildings);

    void setSpecialBlocks(Map<Block, List<BlockPos>> specialBlocks, Map<Block, List<BlockPos>> blueprintSpecialBlocks);

    void open_HireScreen(MinecraftClient client, String screenName, Map<String, IHireInfo> professions, List<String> additionalInfo);

    void sync(
            int colonistsCount,
            BlockPos fortressCenter,
            FortressGamemode gamemode,
            boolean connectedToTheServer,
            int maxColonistsCount,
            int reservedColonistCount,
            boolean campfireEnabled,
            boolean borderEnabled
    );

    void tick(IHoveredBlockProvider fortressClient);

    boolean isConnectedToTheServer();

    BlockPos getPosAppropriateForCenter();

    boolean notInitialized();

    boolean isCenterNotSet();

    void setupFortressCenter();

    void updateRenderer(WorldRenderer worldRenderer);

    List<BlockPos> getBuildingSelection(BlockPos pos);

    boolean isBuildingHovered();

    Optional<IEssentialBuildingInfo> getHoveredBuilding();

    Optional<String> getHoveredBuildingName();

    IClientProfessionManager getProfessionManager();

    int countBuildings(String requirementId);

    boolean gamemodeNeedsInitialization();

    boolean isSurvival();

    IClientFightManager getFightManager();

    int getMaxColonistsCount();

    void reset();

    // getter and setter for state
    void setState(FortressState state);

    FortressState getState();

    List<BuildingHealthRenderInfo> getBuildingHealths();

    boolean isBorderEnabled();

    void openRepairBuildingScreen(UUID buildingId, Map<BlockPos, BlockState> blocksToRepair);

    IClientResourceManager getResourceManager();
}
