package net.remmintan.mods.minefortress.core.interfaces.tasks;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.utils.BuildingHelper;

public interface ITaskBlockInfo {

    default TaskType getType() {
        return getPlacingItem() == null ? TaskType.REMOVE : TaskType.BUILD;
    }

    Item getPlacingItem();
    BlockPos getPos();

    default boolean isInCorrectState(World world) {
        final var pos = getPos();
        if (pos == null) return false;
        return switch (getType()) {
            case REMOVE -> BuildingHelper.canRemoveBlock(world, pos);
            case BUILD -> BuildingHelper.canPlaceBlock(world, pos);
            case REPLACE -> canReplaceBlock(world, pos);
        };
    }

    private boolean canReplaceBlock(World world, BlockPos pos) {
        if (BuildingHelper.canPlaceBlock(world, pos))
            return true;
        final var canRemoveBlock = BuildingHelper.canRemoveBlock(world, pos);
        if (canRemoveBlock) {
            final var blockState = world.getBlockState(pos);
            final var item = getPlacingItem();

            return !(item instanceof BlockItem blockItem) || !blockState.isOf(blockItem.getBlock());
        }
        return false;
    }
}
