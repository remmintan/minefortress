package org.minefortress.entity.ai.controls;

import baritone.api.minefortress.IBlockPosControl;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.minefortress.entity.Colonist;
import org.minefortress.registries.FortressBlocks;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ScaffoldsControl implements IBlockPosControl {

    private final Colonist colonist;
    private static final Block SCAFFOLD_BLOCK = FortressBlocks.SCAFFOLD_OAK_PLANKS;

    private final Queue<BlockPos> results = new ConcurrentLinkedQueue<>();
    private final Queue<BlockTask> blockTasks = new ConcurrentLinkedQueue<>();

    public ScaffoldsControl(Colonist colonist) {
        this.colonist = colonist;
    }

    public void tick() {
        blockTasks.removeIf(BlockTask::execute);
        if(!colonist.getTaskControl().hasTask() && !results.isEmpty()) {
            clearResults();
        }
    }

    @Override
    public void addBlock(BlockPos placePosition) {
        if(!colonist.getTaskControl().hasTask()){
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
            return;
        }
        results.add(placePosition);
    }

    public void clearResults() {
        if(results.isEmpty()) return;
        final World level = colonist.world;
        for(BlockPos result : results) {
            if(level.getBlockState(result).isOf(SCAFFOLD_BLOCK)) {
                level.removeBlock(result, false);
                level.emitGameEvent(this.colonist, GameEvent.BLOCK_DESTROY, result);
            }
        }
        results.clear();
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
