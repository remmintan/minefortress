package net.remmintan.mods.minefortress.core.interfaces.resources;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IClientResourceManager {

    // Screen related
    Set<ItemGroup> getGroups();
    List<ItemStack> getStacks(ItemGroup group);

    boolean hasItems(List<ItemInfo> stacks);

    // Blueprint screen related
    Map<ItemStack, Boolean> getMetRequirements(List<ItemInfo> costs);

    void sync(List<BlockPos> containerPositions);

    int getCountIncludingSimilar(Item item);

}
