package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.resources.gui.smelt.FortressFurnaceScreenHandler;

import java.util.Optional;

public class BlacksmithDailyTask extends AbstractStayNearBlockDailyTask{

    @Override
    public void start(Colonist colonist) {
        colonist.setCurrentTaskDesc("Smelting");
        super.start(colonist);
    }

    @Override
    public boolean canStart(Colonist colonist) {
        return shouldWork(colonist);
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return shouldWork(colonist);
    }

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

    private boolean shouldWork(Colonist colonist){
        return atLeastOneFurnaceIsBurning(colonist) || furnaceScreenIsOpen(colonist);
    }

    private boolean atLeastOneFurnaceIsBurning(Colonist colonist){
        final Optional<FortressServerManager> fortressManagerOpt = colonist.getFortressServerManager();
        if(fortressManagerOpt.isEmpty()) return false;
        final FortressServerManager fortressManager = fortressManagerOpt.get();
        final var furnaces = fortressManager.getSpecialBlocksByType(Blocks.FURNACE, true);
        for(BlockPos pos : furnaces){
            final var furnace = colonist.world.getBlockEntity(pos);
            if(furnace instanceof FurnaceBlockEntity furnaceBlockEntity && furnaceBlockEntity.isBurning()){
                return true;
            }
        }
        return false;
    }

    private boolean furnaceScreenIsOpen(Colonist colonist){
        final var masterPlayerOpt = colonist.getMasterPlayer();
        if(masterPlayerOpt.isEmpty()) return false;
        final var masterPlayer = masterPlayerOpt.get();
        if(!(masterPlayer instanceof ServerPlayerEntity serverPlayer)) return false;
        final var currentScreenHandler = serverPlayer.currentScreenHandler;
        return currentScreenHandler instanceof FortressFurnaceScreenHandler;
    }
}
