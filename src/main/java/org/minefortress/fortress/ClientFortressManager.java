package org.minefortress.fortress;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.networking.c2s.C2SJumpToCampfire;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.fight.ClientFightManager;
import org.minefortress.fortress.resources.client.ClientResourceManagerImpl;
import org.minefortress.professions.ClientProfessionManager;

public final class ClientFortressManager implements IClientFortressManager {

    private final IClientProfessionManager professionManager;
    private final IClientResourceManager resourceManager = new ClientResourceManagerImpl();
    private final IClientFightManager fightManager = new ClientFightManager();
    private boolean connectedToTheServer = false;
    private boolean initialized = false;

    private int colonistsCount = 0;
    private int reservedColonistCount = 0;


    private int maxColonistsCount;

    private FortressState state = FortressState.BUILD_EDITING;

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
            boolean connectedToTheServer,
            int maxColonistsCount,
            int reservedColonistCount) {
        this.colonistsCount = colonistsCount;
        this.connectedToTheServer = connectedToTheServer;
        this.maxColonistsCount = maxColonistsCount;
        this.reservedColonistCount = reservedColonistCount;
        this.initialized = true;
    }

    @Override
    public void tick() {
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
    public IClientProfessionManager getProfessionManager() {
        return professionManager;
    }

    @Override
    public boolean hasRequiredBuilding(ProfessionType type, int level, int minCount) {
        return ClientModUtils.getBuildingsManager().hasRequiredBuilding(type, level, minCount);
    }

    @Override
    public int getTotalColonistsCount() {
        return colonistsCount;
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
            ClientModUtils.getAreasClientManager().getSavedAreasHolder().setNeedRebuild(true);
        }
        if (state == FortressState.BUILD_SELECTION || state == FortressState.BUILD_EDITING) {
            ClientModUtils.getClientTasksHolder().ifPresent(it -> it.setNeedRebuild(true));
        }
    }

    @Override
    public FortressState getState() {
        return this.state;
    }


}
