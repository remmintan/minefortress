package org.minefortress.entity.ai.professions;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.resources.gui.smelt.FortressFurnaceScreenHandler;

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
    @Nullable
    protected BlockPos getBlockPos(Colonist colonist) {
        return colonist.getServerFortressManager()
                .flatMap(it -> it
                        .getSpecialBlocksByType(Blocks.FURNACE, true)
                        .stream()
                        .findFirst()
                )
                .orElse(null);
    }

    @Override
    protected Item getWorkingItem() {
        return Items.COAL;
    }

    private boolean shouldWork(Colonist colonist){
        return atLeastOneFurnaceIsBurning(colonist) || furnaceScreenIsOpen(colonist);
    }

    private boolean atLeastOneFurnaceIsBurning(Colonist colonist){
        return colonist.getServerFortressManager()
                .map(it -> {
                    final var furnaces = it.getSpecialBlocksByType(Blocks.FURNACE, true);
                    for(BlockPos pos : furnaces){
                        final var furnace = colonist.getWorld().getBlockEntity(pos);
                        if(furnace instanceof FurnaceBlockEntity furnaceBlockEntity && furnaceBlockEntity.isBurning()){
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    private boolean furnaceScreenIsOpen(Colonist colonist){
        return colonist.isScreenOpen(FortressFurnaceScreenHandler.class);
    }
}
