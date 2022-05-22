package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.fortress.FortressServerManager;

import java.util.Optional;

public class CrafterDailyTask extends AbstractStayNearBlockDailyTask{

    @Nullable
    protected BlockPos getBlockPos(Colonist colonist) {
        final Optional<FortressServerManager> fortressManagerOpt = colonist.getFortressServerManager();
        if(fortressManagerOpt.isEmpty()) return null;
        final FortressServerManager fortressManager = fortressManagerOpt.get();
        Optional<BlockPos> tablePosOpt = fortressManager.getSpecialBlocksByType(Blocks.CRAFTING_TABLE, true)
                .stream()
                .findFirst();

        if(tablePosOpt.isEmpty()) {
            tablePosOpt = fortressManager.getSpecialBlocksByType(Blocks.CRAFTING_TABLE, false)
                    .stream()
                    .findFirst();
        }

        if(tablePosOpt.isEmpty()) return null;
        return tablePosOpt.get();
    }

}
