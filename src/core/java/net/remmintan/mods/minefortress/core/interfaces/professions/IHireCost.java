package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo;

public interface IHireCost {
    IItemInfo toItemInfo();

    int amount();
}
