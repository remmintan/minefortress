package net.remmintan.mods.minefortress.core.interfaces.professions;

import java.util.List;

public interface IHireInfo {
    String professionId();

    int hireProgress();

    int hireQueue();

    List<IHireCost> cost();
}
