package org.minefortress.tasks.block.info;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;

public class ItemTaskBlockInfo extends TaskBlockInfo {

    private final ItemUsageContext context;

    public ItemTaskBlockInfo(Item placingItem, BlockPos pos, ItemUsageContext context) {
        super(placingItem, pos);
        this.context = context;
    }

    public ItemUsageContext getContext() {
        return context;
    }
}
