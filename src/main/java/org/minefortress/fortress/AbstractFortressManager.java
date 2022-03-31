package org.minefortress.fortress;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

abstract class AbstractFortressManager {

    public BlockState getStateForCampCenter() {
        return Blocks.CAMPFIRE.getDefaultState();
    }
    public abstract boolean hasRequiredBuilding(String requirementId);

}
