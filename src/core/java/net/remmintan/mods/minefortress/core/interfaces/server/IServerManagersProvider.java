package net.remmintan.mods.minefortress.core.interfaces.server;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.remmintan.mods.minefortress.core.interfaces.automation.server.IServerAutomationAreaManager;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.combat.IServerFightManager;
import net.remmintan.mods.minefortress.core.interfaces.professions.IServerProfessionsManager;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IServerTaskManager;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksCreator;

public interface IServerManagersProvider {
    default IServerAutomationAreaManager getAutomationAreaManager() {
        return getManager(IServerAutomationAreaManager.class);
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

    void tick(MinecraftServer server, ServerWorld world);

    void write(NbtCompound tag);

    void read(NbtCompound tag);

    void sync();

    <T extends IServerManager> T getManager(Class<T> managerClass);

}
