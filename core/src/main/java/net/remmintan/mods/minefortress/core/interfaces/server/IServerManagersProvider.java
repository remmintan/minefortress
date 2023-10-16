package net.remmintan.mods.minefortress.core.interfaces.server;

import net.remmintan.mods.minefortress.core.interfaces.automation.server.IServerAutomationAreaManager;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.infuence.IServerInfluenceManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IServerProfessionsManager;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IServerTaskManager;

public interface IServerManagersProvider {

    IServerAutomationAreaManager getAutomationAreaManager();
    IServerInfluenceManager getInfluenceManager();
    IServerProfessionsManager getProfessionsManager();
    IServerBuildingsManager getBuildingsManager();
    IServerResourceManager getResourceManager();
    IServerTaskManager getTaskManager();

}
