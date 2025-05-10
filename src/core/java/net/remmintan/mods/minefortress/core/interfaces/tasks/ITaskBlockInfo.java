package net.remmintan.mods.minefortress.core.interfaces.tasks;

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
        final var type = getType();
        if (type == TaskType.REMOVE) {
            return BuildingHelper.canRemoveBlock(world, pos);
        } else if (type == TaskType.BUILD) {
            return BuildingHelper.canPlaceBlock(world, pos);
        } else {
            throw new IllegalStateException();
        }
    }
}
