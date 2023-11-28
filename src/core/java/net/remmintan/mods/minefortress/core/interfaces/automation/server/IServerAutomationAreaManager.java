package net.remmintan.mods.minefortress.core.interfaces.automation.server;

import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaInfo;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;

import java.util.UUID;
import java.util.stream.Stream;

public interface IServerAutomationAreaManager extends IServerManager {
    void addArea(IAutomationAreaInfo area);
    void removeArea(UUID id);
    Stream<IAutomationArea> getByRequirement(String requirement);
    void sync();
}
