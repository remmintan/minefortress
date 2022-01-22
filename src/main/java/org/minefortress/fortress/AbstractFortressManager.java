package org.minefortress.fortress;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

class AbstractFortressManager {

    public BlockState getStateForCampCenter() {
        return Blocks.CAMPFIRE.getDefaultState();
    }


}
