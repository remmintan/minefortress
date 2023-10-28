package org.minefortress.professions.hire;

import net.remmintan.mods.minefortress.core.interfaces.professions.IHireCost;
import net.remmintan.mods.minefortress.core.interfaces.professions.IHireInfo;

import java.io.Serializable;
import java.util.List;

public record HireInfo(
        String professionId,
        int hireProgress,
        int hireQueue,
        List<IHireCost> cost
) implements IHireInfo, Serializable {}