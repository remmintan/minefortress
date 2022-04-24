package org.minefortress.fortress.resources;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Set;

public interface ClientResourceManager {

    Set<ItemGroup> getGroups();
    boolean hasStacks(List<ItemStack> stacks);
    List<ItemStack> getStacks(ItemGroup group);
    void setItemAmount(Item item, int amount);
    void reset();

}
