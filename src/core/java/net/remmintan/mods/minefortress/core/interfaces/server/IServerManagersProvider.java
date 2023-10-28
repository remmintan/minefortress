package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.entity.LivingEntity;
import net.remmintan.mods.minefortress.core.interfaces.automation.server.IServerAutomationAreaManager;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.infuence.IServerInfluenceManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IServerProfessionsManager;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IServerTaskManager;

import java.util.Optional;
import java.util.UUID;

public interface IServerManagersProvider {
    void killAllPawns();
    Optional<LivingEntity> spawnPawnNearCampfire(UUID id);
    IServerAutomationAreaManager getAutomationAreaManager();
    IServerInfluenceManager getInfluenceManager();
    IServerProfessionsManager getProfessionsManager();
    IServerBuildingsManager getBuildingsManager();
    IServerResourceManager getResourceManager();
    IServerTaskManager getTaskManager();

}
