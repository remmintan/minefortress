package net.remmintan.mods.minefortress.core.interfaces.resources;

import net.remmintan.mods.minefortress.core.dtos.ItemInfo;

import java.util.List;

public interface IResourceManager {

    boolean hasItems(List<ItemInfo> stacks);

}
