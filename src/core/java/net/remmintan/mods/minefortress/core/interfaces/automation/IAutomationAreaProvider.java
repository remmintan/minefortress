package net.remmintan.mods.minefortress.core.interfaces.automation;

import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;

import java.util.stream.Stream;

public interface IAutomationAreaProvider {

    Stream<IAutomationArea> getAutomationAreasByRequirement(String requirementId);

}
