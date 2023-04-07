package org.minefortress.blueprints.interfaces;

import net.minecraft.util.BlockRotation;
import org.minefortress.blueprints.data.StrctureBlockData;

public interface IBlockDataProvider {

    StrctureBlockData getBlockData(String fileName, BlockRotation rotation);
    default void reset() {}
}
