package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
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
        if (colonist.getWorld().isDay()) return false;
        if(colonist.getTarget() != null && colonist.getTarget().isAlive()) return false;
        if(colonist.getTaskControl().hasTask()) return false;
        if(!isFarFromCenter()) return false;

        ServerModUtils.getFortressManager(colonist)
                .flatMap(IServerFortressManager::getRandomPositionAroundCampfire)
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
        return colonist.getWorld().isNight() &&
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
        return ServerModUtils.getFortressManager(colonist)
                .map(it -> {
                    final var fortressPos = colonist.getFortressPos();
                    final var campfireWarmRadius = it.getCampfireWarmRadius();
                    return colonist.getBlockPos().getSquaredDistance(fortressPos) > campfireWarmRadius * campfireWarmRadius;
                })
                .orElse(false);
    }
}
