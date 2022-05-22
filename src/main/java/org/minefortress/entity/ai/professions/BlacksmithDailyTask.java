package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;

import java.util.Optional;

public class BlacksmithDailyTask extends AbstractStayNearBlockDailyTask{
    @Override
    protected BlockPos getBlockPos(Colonist colonist) {
        final Optional<FortressServerManager> fortressManagerOpt = colonist.getFortressServerManager();
        if(fortressManagerOpt.isEmpty()) return null;
        final FortressServerManager fortressManager = fortressManagerOpt.get();
        Optional<BlockPos> tablePosOpt = fortressManager.getSpecialBlocksByType(Blocks.FURNACE, true)
                .stream()
                .findFirst();

        if(tablePosOpt.isEmpty()) return null;
        return tablePosOpt.get();
    }


    @Override
    protected Item getWorkingItem() {
        return Items.COAL;
    }
}
