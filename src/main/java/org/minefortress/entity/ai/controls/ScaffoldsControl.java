package org.minefortress.entity.ai.controls;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.minefortress.tasks.BuildingManager;
import org.minefortress.registries.FortressBlocks;
import org.minefortress.entity.Colonist;

import java.util.ArrayList;
import java.util.List;

public class ScaffoldsControl extends ActionControl {

    private final Colonist colonist;
    private static final BlockItem SCAFFOLD_ITEM = (BlockItem) Items.OAK_PLANKS;
    private static final Block SCAFFOLD_BLOCK = FortressBlocks.SCAFFOLD_OAK_PLANKS;

    private final List<BlockTask> blockTasks = new ArrayList<>();

    public ScaffoldsControl(Colonist colonist) {
        this.colonist = colonist;
    }

    @Override
    public void tick() {
        super.tick();
        blockTasks.removeIf(BlockTask::execute);
    }

    @Override
    protected BlockPos doAction() {
        if(colonist.isOnGround()) {
            colonist.getJumpControl().setActive();
        }

        final BlockPos placePosition = colonist.getBlockPos().down();
        if(BuildingManager.canPlaceScaffold(colonist.world, placePosition)) {
            colonist.putItemInHand(SCAFFOLD_ITEM);
            colonist.world.setBlockState(placePosition, SCAFFOLD_BLOCK.getDefaultState(), 3);
            colonist.world.emitGameEvent(colonist, GameEvent.BLOCK_PLACE, placePosition);

            if(!colonist.isHasTask()){
                final BlockTask blockTask = new BlockTask(
                    () -> {
                        final World level = colonist.world;
                        if (level.getBlockState(placePosition).isOf(SCAFFOLD_BLOCK)) {
                            level.removeBlock(placePosition, false);
                            level.emitGameEvent(this.colonist, GameEvent.BLOCK_DESTROY, placePosition);
                        }
                    }
                );
                blockTasks.add(blockTask);
            }

            return placePosition;
        }
        return null;
    }

    @Override
    protected void clearResults(List<BlockPos> results) {
        final World level = colonist.world;
        for(BlockPos result : results) {
            if(level.getBlockState(result).isOf(SCAFFOLD_BLOCK)) {
                level.removeBlock(result, false);
                level.emitGameEvent(this.colonist, GameEvent.BLOCK_DESTROY, result);
            }
        }
    }

    private static class BlockTask {

        private static final int MAX_TICKS = 25;
        private int ticks = 0;

        private final Runnable action;

        private BlockTask(Runnable action) {
            this.action = action;
        }

        public boolean execute() {
            if(++ticks > MAX_TICKS) {
                action.run();
                return true;
            }
            return false;
        }

    }

}
