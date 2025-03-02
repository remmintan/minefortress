package net.remmintan.mods.minefortress.core.interfaces.automation.server;

import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaInfo;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.ISyncableServerManager;
import net.remmintan.mods.minefortress.core.interfaces.server.ITickableManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IWritableManager;

import java.util.UUID;
import java.util.stream.Stream;

public interface IServerAutomationAreaManager extends IServerManager, ISyncableServerManager, IWritableManager, ITickableManager {
    void addArea(IAutomationAreaInfo area);
    void removeArea(UUID id);

    Stream<IAutomationArea> getByProfessionType(ProfessionType requirement);
}
