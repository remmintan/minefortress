package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.remmintan.mods.minefortress.core.dtos.ItemInfo;

public interface IHireCost {
    ItemInfo toItemInfo();

    int amount();
}
