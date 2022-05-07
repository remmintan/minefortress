package org.minefortress.entity.ai.controls;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.minefortress.blueprints.data.BlueprintBlockData;
import org.minefortress.entity.Colonist;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.ItemTaskBlockInfo;

public class PlaceControl extends PositionedActionControl {

    private final Colonist colonist;

    private int placeCooldown = 0;
    private int failedInteractions = 0;
    private boolean cantPlaceUnderMyself = false;

    public PlaceControl(Colonist colonist) {
        this.colonist = colonist;
    }

    @Override
    public void tick() {
        if(isDone()) return;
        if(!super.canReachTheGoal(colonist) || !colonist.getNavigation().isIdle()) return;

        if(placeCooldown>0) placeCooldown--;

        final BlockPos blockPos = colonist.getBlockPos();
        if(blockPos.equals(goal))
            if(!colonist.isWallAboveTheHead())
                colonist.getJumpControl().setActive();
            else
                cantPlaceUnderMyself = true;
        else
            placeBlock();
    }

    @Override
    public void reset() {
        super.reset();
        failedInteractions = 0;
        cantPlaceUnderMyself = false;
    }

    protected void placeBlock() {
        colonist.lookAtGoal();
        colonist.putItemInHand(item);

        if (placeCooldown <= 0) {
            this.colonist.swingHand(Hand.MAIN_HAND);

            if(taskBlockInfo instanceof ItemTaskBlockInfo)
                place((ItemTaskBlockInfo) taskBlockInfo);
            if(taskBlockInfo instanceof BlockStateTaskBlockInfo)
                place((BlockStateTaskBlockInfo) taskBlockInfo);
        }
    }

    private void place(ItemTaskBlockInfo blockInfo) {
        final ItemUsageContext context = blockInfo.getContext();
        final ActionResult interactionResult = item.useOnBlock(context);

        if(interactionResult == ActionResult.CONSUME || failedInteractions > 15) {
            decreaseResourcesAmount();
            this.reset();
            this.placeCooldown = 6;
        } else {
            failedInteractions++;
        }
    }

    private void place(BlockStateTaskBlockInfo blockInfo) {
        final BlockState stateForPlacement = blockInfo.getState();

        colonist.world.setBlockState(goal, stateForPlacement, 3);
        colonist.world.emitGameEvent(colonist, GameEvent.BLOCK_PLACE, goal);

        decreaseResourcesAmount();

        this.reset();
        this.placeCooldown = 6;
    }

    private void decreaseResourcesAmount() {
        colonist.doActionOnMasterPlayer(p -> {
            final var fortressServerManager = p.getFortressServerManager();
            final var taskControl = colonist.getTaskControl();
            if(fortressServerManager.isSurvival() && taskControl.hasTask()) {
                if (BlueprintBlockData.IGNORED_ITEMS.contains(item)) {
                    fortressServerManager
                            .getServerResourceManager()
                            .removeItemIfExists(item);
                } else {
                    fortressServerManager
                            .getServerResourceManager()
                            .removeReservedItem(taskControl.getTaskId(), item);
                }
            }
        });
    }

    public boolean isCantPlaceUnderMyself() {
        return cantPlaceUnderMyself;
    }
}
