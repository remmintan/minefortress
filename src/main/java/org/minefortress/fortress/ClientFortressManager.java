package org.minefortress.fortress;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.building.BuildingHelper;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.dtos.buildings.BuildingHealthRenderInfo;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IEssentialBuildingInfo;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.client.IHoveredBlockProvider;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireInfo;
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager;
import net.remmintan.mods.minefortress.networking.c2s.C2SJumpToCampfire;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundFortressCenterSetPacket;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundSetGamemodePacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.MineFortressMod;
import org.minefortress.fight.ClientFightManager;
import org.minefortress.fortress.resources.client.ClientResourceManagerImpl;
import org.minefortress.professions.ClientProfessionManager;
import org.minefortress.professions.hire.ClientHireHandler;
import org.minefortress.renderer.gui.fortress.RepairBuildingScreen;
import org.minefortress.renderer.gui.hire.HirePawnScreen;
import org.minefortress.utils.BlockUtils;
import org.minefortress.utils.ModUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ClientFortressManager implements IClientFortressManager {

    private static final Object KEY = new Object();
    private final IClientProfessionManager professionManager;
    private final IClientResourceManager resourceManager = new ClientResourceManagerImpl();
    private final IClientFightManager fightManager = new ClientFightManager();

    private boolean connectedToTheServer = false;
    private boolean initialized = false;

    private BlockPos fortressCenter = null;
    private int colonistsCount = 0;
    private int reservedColonistCount = 0;

    private IEssentialBuildingInfo hoveredBuilding = null;

    private volatile FortressToast setCenterToast;

    private BlockPos posAppropriateForCenter;
    private BlockPos oldPosAppropriateForCenter;

    private LivingEntity selectedPawn;

    private List<IEssentialBuildingInfo> buildings = new ArrayList<>();
    private Map<Block, List<BlockPos>> specialBlocks = new HashMap<>();
    private Map<Block, List<BlockPos>> blueprintsSpecialBlocks = new HashMap<>();

    private FortressGamemode gamemode;

    private int maxColonistsCount;

    private FortressState state = FortressState.BUILD;

    private boolean campfireEnabled = true;
    private boolean borderEnabled = true;

    public ClientFortressManager() {
        professionManager = new ClientProfessionManager(
                () -> ((IClientManagersProvider) MinecraftClient.getInstance())
                        .get_ClientFortressManager()
        );
    }

    @Override
    public void select(LivingEntity colonist) {
        if(state == FortressState.COMBAT) {
            final var mouse = MinecraftClient.getInstance().mouse;
            final var selectionManager = fightManager.getSelectionManager();
            selectionManager.startSelection(mouse.getX(), mouse.getY(), colonist.getPos());
            selectionManager.updateSelection(mouse.getX(), mouse.getY(), colonist.getPos());
            selectionManager.endSelection();

            selectedPawn = null;
            return;
        }
        this.selectedPawn = colonist;
    }

    @Override
    public void jumpToCampfire() {
        final var packet = new C2SJumpToCampfire();
        FortressClientNetworkHelper.send(C2SJumpToCampfire.CHANNEL, packet);
    }

    @Override
    public void updateBuildings(List<IEssentialBuildingInfo> buildings) {
        this.buildings = buildings;
    }

    @Override
    public void setSpecialBlocks(Map<Block, List<BlockPos>> specialBlocks, Map<Block, List<BlockPos>> blueprintSpecialBlocks) {
        this.specialBlocks = specialBlocks;
        this.blueprintsSpecialBlocks = blueprintSpecialBlocks;
    }

    @Override
    public boolean isSelectingColonist() {
        return selectedPawn != null && state == FortressState.BUILD;
    }

    @Override
    public LivingEntity getSelectedPawn() {
        return selectedPawn;
    }

    @Override
    public void stopSelectingColonist() {
        this.selectedPawn = null;
    }

    @Override
    public int getReservedPawnsCount() {
        return reservedColonistCount;
    }

    @Override
    public void sync(
            int colonistsCount,
            BlockPos fortressCenter,
            FortressGamemode gamemode,
            boolean connectedToTheServer,
            int maxColonistsCount,
            int reservedColonistCount,
            boolean campfireEnabled,
            boolean borderEnabled
    ) {
        this.colonistsCount = colonistsCount;
        this.fortressCenter = fortressCenter;
        this.gamemode = gamemode;
        this.connectedToTheServer = connectedToTheServer;
        this.maxColonistsCount = maxColonistsCount;
        this.reservedColonistCount = reservedColonistCount;
        this.campfireEnabled = campfireEnabled;
        this.borderEnabled = borderEnabled;
        this.initialized = true;
    }

    @Override
    public void tick(IHoveredBlockProvider fortressClient) {
        if(isSelectingColonist() && selectedPawn.isDead()) stopSelectingColonist();

        final MinecraftClient client = (MinecraftClient) fortressClient;
        if(
                client.world == null ||
                client.interactionManager == null ||
                client.interactionManager.getCurrentGameMode() != MineFortressMod.FORTRESS
        ) {
            synchronized (KEY) {
                if(setCenterToast != null) {
                    setCenterToast.hide();
                    setCenterToast = null;
                }
            }

            posAppropriateForCenter = null;
            return;
        }
        if(!initialized) return;
        if(isCenterNotSet()) {
            synchronized (KEY) {
                if(setCenterToast == null) {
                    this.setCenterToast = new FortressToast("Set up your Fortress", "Right-click to place", Items.CAMPFIRE);
                    client.getToastManager().add(setCenterToast);
                }
            }

            final BlockPos hoveredBlockPos = fortressClient.get_HoveredBlockPos();
            if(hoveredBlockPos!=null && !hoveredBlockPos.equals(BlockPos.ORIGIN)) {
                if(hoveredBlockPos.equals(oldPosAppropriateForCenter)) return;

                BlockPos cursor = hoveredBlockPos;
                while (!BuildingHelper.canPlaceBlock(client.world, cursor))
                    cursor = cursor.up();

                while (BuildingHelper.canPlaceBlock(client.world, cursor.down()))
                    cursor = cursor.down();

                posAppropriateForCenter = cursor.toImmutable();
            }
        }
    }

    @Override
    public void open_HireScreen(MinecraftClient client, String screenName, Map<String, IHireInfo> professions) {
        final var handler = new ClientHireHandler(screenName, professions);
        final var screen = new HirePawnScreen(handler);
        client.setScreen(screen);
    }

    @Override
    public boolean isConnectedToTheServer() {
        return initialized && connectedToTheServer;
    }

    @Override
    public BlockPos getPosAppropriateForCenter() {
        return posAppropriateForCenter;
    }

    @Override
    public boolean notInitialized() {
        return !initialized;
    }

    @Override
    public boolean isCenterNotSet() {
        return initialized && fortressCenter == null && this.gamemode != FortressGamemode.NONE;
    }

    @Override
    public void setupFortressCenter() {
        if(fortressCenter!=null) throw new IllegalStateException("Fortress center already set");
        this.setCenterToast.hide();
        this.setCenterToast = null;
        fortressCenter = posAppropriateForCenter;
        posAppropriateForCenter = null;
        final ServerboundFortressCenterSetPacket serverboundFortressCenterSetPacket = new ServerboundFortressCenterSetPacket(fortressCenter);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SET_CENTER, serverboundFortressCenterSetPacket);

        final MinecraftClient client = MinecraftClient.getInstance();
        final WorldRenderer worldRenderer = client.worldRenderer;


        if(worldRenderer!=null) {
            worldRenderer.scheduleBlockRenders(fortressCenter.getX(), fortressCenter.getY(), fortressCenter.getZ());
            worldRenderer.scheduleTerrainUpdate();
        }
    }

    @Override
    public void updateRenderer(WorldRenderer worldRenderer) {
        if(oldPosAppropriateForCenter == posAppropriateForCenter) return;
        final BlockPos posAppropriateForCenter = this.getPosAppropriateForCenter();
        if(posAppropriateForCenter != null) {
            oldPosAppropriateForCenter = posAppropriateForCenter;
            final BlockPos start = posAppropriateForCenter.add(-2, -2, -2);
            final BlockPos end = posAppropriateForCenter.add(2, 2, 2);
            worldRenderer.scheduleBlockRenders(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
            worldRenderer.scheduleTerrainUpdate();
        }
    }

    @Override
    public List<BlockPos> getBuildingSelection(BlockPos pos) {
        for(IEssentialBuildingInfo building : buildings){
            final BlockPos start = building.getStart();
            final BlockPos end = building.getEnd();
            if(BlockUtils.isPosBetween(pos, start, end)){
                hoveredBuilding = building;
                return StreamSupport
                        .stream(BlockPos.iterate(start, end).spliterator(), false)
                        .map(BlockPos::toImmutable)
                        .collect(Collectors.toList());
            }
        }
        hoveredBuilding = null;
        return Collections.emptyList();
    }

    @Override
    public boolean isBuildingHovered() {
        return hoveredBuilding != null;
    }

    @Override
    public Optional<IEssentialBuildingInfo> getHoveredBuilding() {
        return Optional.ofNullable(hoveredBuilding);
    }

    @Override
    public Optional<String> getHoveredBuildingName() {
        return getHoveredBuilding()
                .flatMap(IEssentialBuildingInfo::getBlueprintId)
                .flatMap(it -> ModUtils.getBlueprintManager().getBlueprintMetadataManager().getByBlueprintId(it))
                .map(IBlueprintMetadata::getName);
    }

    @Override
    public IClientProfessionManager getProfessionManager() {
        return professionManager;
    }

    @Override
    public boolean hasRequiredBuilding(String requirementId, int minCount) {
        final var requiredBuilding = buildings.stream()
                .filter(b -> b.getRequirementId().equals(requirementId));
        if(requirementId.startsWith("miner") || requirementId.startsWith("lumberjack") || requirementId.startsWith("warrior")) {
            return requiredBuilding
                    .mapToLong(it -> it.getBedsCount() * 10)
                    .sum() > minCount;
        }
        final var count = requiredBuilding.count();
        if(requirementId.equals("shooting_gallery"))
            return count * 10 > minCount;

        if(requirementId.startsWith("farm"))
            return count * 5 > minCount;


        return count > minCount;
    }

    @Override
    public int countBuildings(String requirementId) {
        return (int) buildings.stream()
                .filter(b -> b.getRequirementId().equals(requirementId))
                .count();
    }

    @Override
    public boolean hasRequiredBlock(Block block, boolean blueprint, int minCount) {
        if(blueprint)
            return this.blueprintsSpecialBlocks.getOrDefault(block, Collections.emptyList()).size() > minCount;
        else
            return this.specialBlocks.getOrDefault(block, Collections.emptyList()).size() > minCount;
    }

    @Override
    public int getTotalColonistsCount() {
        return colonistsCount;
    }

    @Override
    public void setGamemode(FortressGamemode gamemode) {
        if(gamemode == null) throw new IllegalArgumentException("Gamemode cannot be null");
        if(gamemode == FortressGamemode.NONE) throw new IllegalArgumentException("Gamemode cannot be NONE");
        final ServerboundSetGamemodePacket serverboundSetGamemodePacket = new ServerboundSetGamemodePacket(gamemode);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SET_GAMEMODE, serverboundSetGamemodePacket);
    }

    @Override
    public boolean gamemodeNeedsInitialization() {
        return this.initialized && this.gamemode == FortressGamemode.NONE;
    }

    public boolean isCreative() {
        return this.gamemode == FortressGamemode.CREATIVE;
    }

    @Override
    public boolean isSurvival() {
        return this.gamemode != null && this.gamemode == FortressGamemode.SURVIVAL;
    }

    public IClientResourceManager getResourceManager() {
        return resourceManager;
    }

    @Override
    public IClientFightManager getFightManager() {
        return fightManager;
    }

    @Override
    public int getMaxColonistsCount() {
        return maxColonistsCount;
    }

    @Override
    public void reset() {
        this.initialized = false;
        this.selectedPawn = null;
        this.state= FortressState.BUILD;
    }

    // getter and setter for state
    @Override
    public void setState(FortressState state) {
        this.state = state;
        if(state == FortressState.AREAS_SELECTION) {
            ModUtils.getAreasClientManager().getSavedAreasHolder().setNeedRebuild(true);
        }
        if(state == FortressState.BUILD) {
            ModUtils.getClientTasksHolder().ifPresent(it -> it.setNeedRebuild(true));
        }
    }

    @Override
    public FortressState getState() {
        return this.state;
    }

    @Override
    public List<BuildingHealthRenderInfo> getBuildingHealths() {
        return switch (this.getState()) {
            case COMBAT -> buildings
                    .stream()
                    .filter(it -> it.getHealth() < 100)
                    .map(this::buildingToHealthRenderInfo)
                    .toList();
            case BUILD -> buildings
                    .stream()
                    .filter(it -> it.getHealth() < 33)
                    .map(this::buildingToHealthRenderInfo)
                    .toList();
            default -> Collections.emptyList();
        };
    }

    private BuildingHealthRenderInfo buildingToHealthRenderInfo(IEssentialBuildingInfo buildingInfo) {
        final var start = buildingInfo.getStart();
        final var end = buildingInfo.getEnd();

        final var maxY = Math.max(start.getY(), end.getY());
        final var centerX = (start.getX() + end.getX()) / 2;
        final var centerZ = (start.getZ() + end.getZ()) / 2;

        final var center = new Vec3d(centerX, maxY, centerZ);
        final var health = buildingInfo.getHealth();

        return new BuildingHealthRenderInfo(center, health);
    }

    @Override
    public boolean isBorderEnabled() {
        return borderEnabled;
    }

    @Override
    public void openRepairBuildingScreen(UUID buildingId, Map<BlockPos, BlockState> blocksToRepair) {
        MinecraftClient.getInstance().setScreen(new RepairBuildingScreen(buildingId, blocksToRepair, resourceManager));
    }
}
