package net.remmintan.mods.minefortress.core.interfaces.resources;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IClientResourceManager extends IResourceManager {

    Set<ItemGroup> getGroups();
    boolean hasStacks(List<ItemStack> stacks);

    Map<ItemInfo, Boolean> getMetRequirements(List<ItemInfo> costs);
    List<ItemStack> getStacks(ItemGroup group);
    void setItemAmount(Item item, int amount);
    int getItemAmount(Item item);
    void reset();

    int getCountIncludingSimilars(Item item);

}
