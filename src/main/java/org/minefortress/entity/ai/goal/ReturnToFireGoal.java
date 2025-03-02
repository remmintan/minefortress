package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.minefortress.entity.Colonist;

import java.util.EnumSet;

public class ReturnToFireGoal extends AbstractFortressGoal {

    private BlockPos positionAroundCampfire;

    public ReturnToFireGoal(Colonist colonist) {
        super(colonist);
        super.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if(colonist.getTarget() != null && colonist.getTarget().isAlive()) return false;
        if(colonist.getTaskControl().hasTask()) return false;
        if(!isFarFromCenter()) return false;

        final var fortressManager = ServerModUtils.getFortressManager(colonist);
        fortressManager.getRandomPositionAroundCampfire()
                .ifPresent(it -> positionAroundCampfire = it);

        return  positionAroundCampfire != null;
    }

    @Override
    public void start() {
        super.start();
        colonist.getMovementHelper().goTo(positionAroundCampfire, Colonist.SLOW_MOVEMENT_SPEED);
        this.colonist.setCurrentTaskDesc("Staying near campfire");
    }

    @Override
    public boolean shouldContinue() {
        return
                !colonist.getTaskControl().hasTask() &&
                !colonist.getMovementHelper().isStuck() &&
                (isFarFromCenter() || colonist.getMovementHelper().stillTryingToReachGoal());
    }

    @Override
    public void stop() {
        super.stop();
        colonist.getMovementHelper().reset();
        this.positionAroundCampfire = null;
    }

    private boolean isFarFromCenter() {
        final var fortressManager = ServerModUtils.getFortressManager(colonist);
        final BlockPos fortressCenter = fortressManager.getFortressCenter();
        if (fortressCenter == null) return false;
        final var distanseToCenter = Math.sqrt(colonist.squaredDistanceTo(fortressCenter.getX(), fortressCenter.getY(), fortressCenter.getZ()));
        return distanseToCenter > fortressManager.getCampfireWarmRadius();
    }
}
