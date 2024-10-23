package net.remmintan.mods.minefortress.core.interfaces.automation;

import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;

import java.util.stream.Stream;

public interface IAutomationAreaProvider {

    Stream<IAutomationArea> getAutomationAreaByProfessionType(ProfessionType requirementId);

}
