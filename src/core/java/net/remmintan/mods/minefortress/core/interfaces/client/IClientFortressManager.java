package net.remmintan.mods.minefortress.core.interfaces.client;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.interfaces.IFortressManager;
import net.remmintan.mods.minefortress.core.interfaces.combat.IClientFightManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IClientProfessionManager;
import net.remmintan.mods.minefortress.core.interfaces.resources.IClientResourceManager;

public interface IClientFortressManager extends IFortressManager {

    void jumpToCampfire();
    void sync(
            int colonistsCount,
            BlockPos fortressCenter,
            FortressGamemode gamemode,
            boolean connectedToTheServer,
            int maxColonistsCount,
            int reservedColonistCount
    );

    void tick(IHoveredBlockProvider fortressClient);

    boolean isConnectedToTheServer();

    boolean notInitialized();

    boolean isCenterNotSet();

    void setupFortressCenter(BlockPos pos);

    IClientProfessionManager getProfessionManager();

    boolean gamemodeNeedsInitialization();

    boolean isSurvival();

    IClientFightManager getFightManager();

    int getMaxColonistsCount();

    void reset();

    // getter and setter for state
    void setState(FortressState state);

    FortressState getState();

    IClientResourceManager getResourceManager();
}
