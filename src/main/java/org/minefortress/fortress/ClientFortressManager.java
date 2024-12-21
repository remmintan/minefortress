package org.minefortress.fortress;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.client.IHoveredBlockProvider;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireInfo;
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.c2s.C2SJumpToCampfire;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundSetGamemodePacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.fight.ClientFightManager;
import org.minefortress.fortress.resources.client.ClientResourceManagerImpl;
import org.minefortress.professions.ClientProfessionManager;
import org.minefortress.professions.hire.ClientHireHandler;
import org.minefortress.renderer.gui.hire.HirePawnScreen;

import java.util.List;
import java.util.Map;

public final class ClientFortressManager implements IClientFortressManager {

    private final IClientProfessionManager professionManager;
    private final IClientResourceManager resourceManager = new ClientResourceManagerImpl();
    private final IClientFightManager fightManager = new ClientFightManager();
    private boolean connectedToTheServer = false;
    private boolean initialized = false;

    private BlockPos fortressCenter = null;
    private int colonistsCount = 0;
    private int reservedColonistCount = 0;

    private FortressGamemode gamemode;

    private int maxColonistsCount;

    private FortressState state = FortressState.BUILD_SELECTION;

    public ClientFortressManager() {
        professionManager = new ClientProfessionManager(
                () -> ((IClientManagersProvider) MinecraftClient.getInstance())
                        .get_ClientFortressManager()
        );
    }

    @Override
    public void jumpToCampfire() {
        final var packet = new C2SJumpToCampfire();
        FortressClientNetworkHelper.send(C2SJumpToCampfire.CHANNEL, packet);
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
            int reservedColonistCount) {
        this.colonistsCount = colonistsCount;
        this.fortressCenter = fortressCenter;
        this.gamemode = gamemode;
        this.connectedToTheServer = connectedToTheServer;
        this.maxColonistsCount = maxColonistsCount;
        this.reservedColonistCount = reservedColonistCount;
        this.initialized = true;
    }

    @Override
    public void tick(IHoveredBlockProvider fortressClient) {
        if (!initialized) return;

        resetBuildEditState();

        if (isCenterNotSet()) {
            final var blueprintManager = CoreModUtils.getMineFortressManagersProvider().get_BlueprintManager();
            if (!blueprintManager.isSelecting()) {
                blueprintManager.select("campfire");
            } else {
                final var id = blueprintManager.getSelectedStructure().getId();
                if (!id.equals("campfire")) {
                    blueprintManager.select("campfire");
                }
            }
        }
    }

    private void resetBuildEditState() {
        if (this.state == FortressState.BUILD_EDITING && !CoreModUtils.getMineFortressManagersProvider().get_PawnsSelectionManager().hasSelected()) {
            this.state = FortressState.BUILD_SELECTION;
        }
    }

    @Override
    public void open_HireScreen(MinecraftClient client, String screenName, Map<String, IHireInfo> professions, List<String> additionalInfo) {
        final var handler = new ClientHireHandler(screenName, professions, additionalInfo);
        final var screen = new HirePawnScreen(handler);
        client.setScreen(screen);
    }

    @Override
    public boolean isConnectedToTheServer() {
        return initialized && connectedToTheServer;
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
    public void setupFortressCenter(BlockPos pos) {
        if (fortressCenter != null) throw new IllegalStateException("Fortress center already set");
        fortressCenter = pos;
    }


    @Override
    public IClientProfessionManager getProfessionManager() {
        return professionManager;
    }

    @Override
    public boolean hasRequiredBuilding(ProfessionType type, int level, int minCount) {
        return CoreModUtils.getBuildingsManager().hasRequiredBuilding(type, level, minCount);
    }

    @Override
    public int getTotalColonistsCount() {
        return colonistsCount;
    }

    @Override
    public void setGamemode(FortressGamemode gamemode) {
        if (gamemode == null) throw new IllegalArgumentException("Gamemode cannot be null");
        if (gamemode == FortressGamemode.NONE) throw new IllegalArgumentException("Gamemode cannot be NONE");
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
        this.state = FortressState.BUILD_SELECTION;
    }

    // getter and setter for state
    @Override
    public void setState(FortressState state) {
        this.state = state;
        if (state == FortressState.AREAS_SELECTION) {
            CoreModUtils.getAreasClientManager().getSavedAreasHolder().setNeedRebuild(true);
        }
        if (state == FortressState.BUILD_SELECTION || state == FortressState.BUILD_EDITING) {
            CoreModUtils.getClientTasksHolder().ifPresent(it -> it.setNeedRebuild(true));
        }
    }

    @Override
    public FortressState getState() {
        return this.state;
    }


}
