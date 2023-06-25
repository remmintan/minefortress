package org.minefortress.fortress.automation;

import java.util.stream.Stream;

public interface IAutomationAreaProvider {

    Stream<IAutomationArea> getAutomationAreasByRequirement(String requirementId);

}
