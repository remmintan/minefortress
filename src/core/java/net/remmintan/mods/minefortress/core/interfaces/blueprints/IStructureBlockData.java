package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;

import java.util.List;
import java.util.Map;

public interface IStructureBlockData {
    boolean hasLayer(BlueprintDataLayer layer);

    Map<BlockPos, BlockState> getLayer(BlueprintDataLayer layer);

    Vec3i getSize();

    List<ItemInfo> getStacks();
}
