package org.minefortress.tasks.block.info;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public abstract class TaskBlockInfo {

    private final Item placingItem;
    private final BlockPos pos;

    public TaskBlockInfo(Item placingItem, BlockPos pos) {
        this.placingItem = placingItem;
        this.pos = pos;
    }

    public Item getPlacingItem() {
        return placingItem;
    }

    public BlockPos getPos() {
        return pos;
    }




}
