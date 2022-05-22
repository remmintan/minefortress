package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreenHandler;

import java.util.Optional;

public class CrafterDailyTask extends AbstractStayNearBlockDailyTask{

    private int ticksAfterTableClose = 0;

    @Override
    public boolean canStart(Colonist colonist) {
        return craftingTableMenuOpened(colonist);
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

    private boolean craftingTableMenuOpened(Colonist colonsit) {
        final var masterPlayerOpt = colonsit.getMasterPlayer();
        if(masterPlayerOpt.isEmpty()) return false;
        final var masterPlayer = masterPlayerOpt.get();
        if (masterPlayer instanceof ServerPlayerEntity serverPlayer) {
            final var currentScreenHandler = serverPlayer.currentScreenHandler;
            return currentScreenHandler instanceof FortressCraftingScreenHandler;
        }
        return false;
    }

}
