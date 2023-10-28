package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreenHandler;

public class CrafterDailyTask extends AbstractStayNearBlockDailyTask{

    private int ticksAfterTableClose = 0;

    @Override
    public boolean canStart(Colonist colonist) {
        return craftingTableMenuOpened(colonist);
    }


    @Override
    public void start(Colonist colonist) {
        colonist.setCurrentTaskDesc("Crafting");
        super.start(colonist);
    }

    @Override
    public void tick(Colonist colonist) {
        super.tick(colonist);
        if(craftingTableMenuOpened(colonist)) {
            ticksAfterTableClose = 400;
        } else {
            ticksAfterTableClose--;
        }
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return super.shouldContinue(colonist) && ticksAfterTableClose > 0 || craftingTableMenuOpened(colonist);
    }

    @Override
    public void stop(Colonist colonist) {
        super.stop(colonist);
        this.ticksAfterTableClose = 0;
    }

    @Nullable
    protected BlockPos getBlockPos(Colonist colonist) {
         return colonist
                    .getServerFortressManager()
                    .map(it -> it
                            .getSpecialBlocksByType(Blocks.CRAFTING_TABLE, true)
                            .stream()
                            .findFirst()
                            .orElseGet(() -> it
                                    .getSpecialBlocksByType(Blocks.CRAFTING_TABLE, false)
                                    .stream()
                                    .findFirst()
                                    .orElse(null)
                            )
                    ).orElse(null);
    }

    private boolean craftingTableMenuOpened(Colonist colonsit) {
        return colonsit.isScreenOpen(FortressCraftingScreenHandler.class);
    }

}
