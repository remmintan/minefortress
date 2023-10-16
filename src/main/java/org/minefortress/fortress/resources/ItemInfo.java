package org.minefortress.fortress.resources;

import net.minecraft.item.Item;
import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo;

public record ItemInfo(Item item, int amount) implements IItemInfo {}
