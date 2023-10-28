package org.minefortress.tasks.block.info;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;

public abstract class TaskBlockInfo implements ITaskBlockInfo {

    private final Item placingItem;
    private final BlockPos pos;

    public TaskBlockInfo(Item placingItem, BlockPos pos) {
        this.placingItem = placingItem;
        this.pos = pos;
    }

    @Override
    public Item getPlacingItem() {
        return placingItem;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }




}
