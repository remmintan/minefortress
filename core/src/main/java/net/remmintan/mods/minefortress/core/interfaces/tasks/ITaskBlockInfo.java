package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public interface ITaskBlockInfo {
    Item getPlacingItem();

    BlockPos getPos();
}
