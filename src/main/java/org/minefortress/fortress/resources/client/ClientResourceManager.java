package org.minefortress.fortress.resources.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.List;
import java.util.Set;

public interface ClientResourceManager {

    Set<ItemGroup> getGroups();
    boolean hasStacks(List<ItemStack> stacks);
    boolean hasItems(List<ItemInfo> stacks);
    boolean hasItem(ItemInfo item, List<ItemInfo> items);
    List<ItemStack> getStacks(ItemGroup group);
    void setItemAmount(Item item, int amount);
    void reset();
    List<ItemStack> getAllStacks();

}
