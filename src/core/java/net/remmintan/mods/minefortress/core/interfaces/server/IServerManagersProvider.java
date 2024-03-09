package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.entity.LivingEntity;
import net.remmintan.mods.minefortress.core.interfaces.automation.server.IServerAutomationAreaManager;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.combat.IServerFightManager;
import net.remmintan.mods.minefortress.core.interfaces.infuence.IServerInfluenceManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IServerProfessionsManager;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IServerTaskManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksCreator;

import java.util.Optional;
import java.util.UUID;

public interface IServerManagersProvider {
    void killAllPawns();
    Optional<LivingEntity> spawnPawnNearCampfire(UUID id);
    default IServerAutomationAreaManager getAutomationAreaManager() {
        return getManager(IServerAutomationAreaManager.class);
    }
    default IServerInfluenceManager getInfluenceManager() {
        return getManager(IServerInfluenceManager.class);
    }
    default IServerProfessionsManager getProfessionsManager() {
        return getManager(IServerProfessionsManager.class);
    }
    default IServerBuildingsManager getBuildingsManager() {
        return getManager(IServerBuildingsManager.class);
    }
    default IServerResourceManager getResourceManager() {
        return getManager(IServerResourceManager.class);
    }
    default IServerTaskManager getTaskManager() {
        return getManager(IServerTaskManager.class);
    }
    default ITasksCreator getTasksCreator() {
        return getManager(ITasksCreator.class);
    }
    default IServerFightManager getFightManager() {
        return getManager(IServerFightManager.class);
    }

    <T> T getManager(Class<T> managerClass);

}
