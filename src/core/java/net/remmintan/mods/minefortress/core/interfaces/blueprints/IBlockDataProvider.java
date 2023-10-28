package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.util.BlockRotation;

public interface IBlockDataProvider {

    IStructureBlockData getBlockData(String fileName, BlockRotation rotation);
    default void reset() {}
}
