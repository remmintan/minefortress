package org.minefortress.entity.ai.controls;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.event.GameEvent;
import org.minefortress.entity.Colonist;

public class DigControl extends PositionedActionControl {

    private final Colonist colonist;
    private final ServerWorld level;

    private float destroyProgress = 0.0f;
    private int destroyTicks = 0;

    public DigControl(Colonist colonist, ServerWorld level) {
        this.colonist = colonist;
        this.level = level;
    }

    @Override
    public void tick() {
        if(isDone()) return;
        if(!super.canReachTheGoal(colonist) || !colonist.getNavigation().isIdle()) return;

        if(act()) {
            reset();
        }
    }

    private boolean act() {
        putProperItemInHand();

        if(destroyProgress >= 1.0f){
            this.destroyProgress = 0f;
            level.breakBlock(this.goal, false, this.colonist);
            level.emitGameEvent(this.colonist, GameEvent.BLOCK_DESTROY, goal);
            return true;
        } else {
            this.destroyProgress += this.getDestroyProgress(level.getBlockState(goal), colonist, level, goal);
            this.colonist.lookAtGoal();
            if(++destroyTicks % 4 == 0) {
                this.colonist.swingHand(Hand.MAIN_HAND);
            }
            return false;
        }
    }

    private void putProperItemInHand() {
        final BlockState blockState = level.getBlockState(goal);
        Item item = null;
        if(blockState.isIn(BlockTags.PICKAXE_MINEABLE)) {
            item = blockState.isIn(BlockTags.NEEDS_DIAMOND_TOOL) ? Items.DIAMOND_PICKAXE : Items.IRON_PICKAXE;
        } else if (blockState.isIn(BlockTags.SHOVEL_MINEABLE)) {
            item = blockState.isIn(BlockTags.NEEDS_DIAMOND_TOOL) ? Items.DIAMOND_SHOVEL : Items.IRON_SHOVEL;
        } else if (blockState.isIn(BlockTags.AXE_MINEABLE)) {
            item = blockState.isIn(BlockTags.NEEDS_DIAMOND_TOOL) ? Items.DIAMOND_AXE : Items.IRON_AXE;
        } else if (blockState.isIn(BlockTags.HOE_MINEABLE)) {
            item = blockState.isIn(BlockTags.NEEDS_DIAMOND_TOOL) ? Items.DIAMOND_HOE : Items.IRON_HOE;
        }

        colonist.putItemInHand(item);
    }

    private float getDestroyProgress(BlockState p_60466_, Colonist p_60467_, StructureWorldAccess p_60468_, BlockPos p_60469_) {
        float f = p_60466_.getHardness(p_60468_, p_60469_);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            return p_60467_.getDestroySpeed(p_60466_) / f / 100f;
        }
    }

    @Override
    public void reset() {
        super.reset();
        destroyProgress = 0.0f;
        destroyTicks = 0;
    }
}
