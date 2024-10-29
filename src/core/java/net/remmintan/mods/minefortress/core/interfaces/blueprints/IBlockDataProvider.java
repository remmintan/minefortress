package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.util.BlockRotation;

public interface IBlockDataProvider {

    IStructureBlockData getBlockData(String blueprintId, BlockRotation rotation);
    default void reset() {}
}
