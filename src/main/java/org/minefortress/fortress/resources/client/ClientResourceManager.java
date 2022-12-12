package org.minefortress.fortress.resources.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.minefortress.fortress.resources.FortressResourceManager;
import org.minefortress.fortress.resources.ItemInfo;

import java.util.List;
import java.util.Set;

public interface ClientResourceManager extends FortressResourceManager {

    Set<ItemGroup> getGroups();
    boolean hasStacks(List<ItemStack> stacks);
    boolean hasItem(ItemInfo item, List<ItemInfo> items);
    List<ItemStack> getStacks(ItemGroup group);
    void setItemAmount(Item item, int amount);
    int getItemAmount(Item item);
    void reset();
    List<ItemStack> getAllStacks();

}
