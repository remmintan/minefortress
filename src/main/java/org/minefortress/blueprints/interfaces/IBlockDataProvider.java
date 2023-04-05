package org.minefortress.blueprints.interfaces;

import net.minecraft.util.BlockRotation;
import org.minefortress.blueprints.data.BlueprintBlockData;

public interface IBlockDataProvider {

    BlueprintBlockData getBlockData(String fileName, BlockRotation rotation);
}
