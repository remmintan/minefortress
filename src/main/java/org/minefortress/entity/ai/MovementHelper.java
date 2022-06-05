package org.minefortress.entity.ai;

import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;

public class MovementHelper {

    private final ColonistNavigation navigation;
    private final Colonist colonist;
    private BlockPos workGoal;

    private boolean cantFindPath;

    private int attemptsToCalcPath = 0;

    private BlockPos lastPos;
    private int stuckOnSamePosition = 0;

    public MovementHelper(ColonistNavigation navigation, Colonist colonist) {
        this.navigation = navigation;
        this.colonist = colonist;
    }

    public void reset() {
        this.navigation.stop();
        this.workGoal = null;
        this.cantFindPath = false;
        this.stuckOnSamePosition = 0;
        this.colonist.setAllowToPlaceBlockFromFarAway(false);
    }

    public BlockPos getWorkGoal() {
        return workGoal;
    }

    public void set(BlockPos goal) {
        if(goal != null && goal.equals(workGoal))
            this.colonist.getNavigation().stop();
        this.workGoal = goal;
        this.cantFindPath = false;
        this.stuckOnSamePosition = 0;

        this.colonist.setAllowToPlaceBlockFromFarAway(false);
    }

    public boolean hasReachedWorkGoal() {
        final boolean withinDistance =
                this.workGoal.isWithinDistance(this.colonist.getBlockPos().up(), Colonist.WORK_REACH_DISTANCE)
                || this.colonist.isAllowToPlaceBlockFromFarAway();

        return
                withinDistance &&
                this.navigation.isIdle() &&
                colonist.fallDistance<=1;
    }

    public boolean stillTryingToReachGoal() {
        return !this.navigation.isIdle();
    }

    public void tick() {
        if(workGoal == null || hasReachedWorkGoal()) return;
        checkStuck();
        if(colonist.isSubmergedIn(FluidTags.WATER) || colonist.world.getBlockState(colonist.getBlockPos().down()).isOf(Blocks.WATER)) {
            colonist.getJumpControl().setActive();
        }
        if(!this.navigation.isIdle() || cantFindPath) return;

        final NodeMaker nodeEvaluator = (NodeMaker) navigation.getNodeMaker();

        nodeEvaluator.setWallClimbMode(true);
        final Path path = navigation.findPathTo(workGoal, 3);
        nodeEvaluator.setWallClimbMode(false);

        if(path != null && (path.reachesTarget() || navigation.getCurrentPath() == null || !navigation.getCurrentPath().equals(path))) {
            navigation.startMovingAlong(path, 1.75f / colonist.getHungerMultiplier());
        }

        if (path == null) {
            attemptsToCalcPath++;
            if(attemptsToCalcPath > 10) {
                this.colonist.setAllowToPlaceBlockFromFarAway(true);
            }
        }
    }

    public void checkStuck() {
        final BlockPos currentPos = colonist.getBlockPos();
        if(currentPos.equals(lastPos)) {
            stuckOnSamePosition++;
        } else {
            stuckOnSamePosition = 0;
        }
        lastPos = currentPos.toImmutable();

        int maxStuckTime = colonist.isTouchingWater()? 200: 50;
        if(stuckOnSamePosition > maxStuckTime) {
            this.cantFindPath = true;
            stuckOnSamePosition = 0;
        }

        if(navigation.isCantCreateScaffold()) {
            this.cantFindPath = true;
            stuckOnSamePosition = 0;
        }

        if(colonist.getPlaceControl().isCantPlaceUnderMyself()) {
            this.cantFindPath = true;
            stuckOnSamePosition = 0;
        }
    }

    public boolean isCantFindPath() {
        return cantFindPath;
    }
}
