package org.minefortress.entity.ai.goal;

import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;

import java.util.Optional;

public class ReturnToFireGoal extends AbstractFortressGoal {


    public ReturnToFireGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        if(!notInCombat()) return false;
        if(!isNight()) {
            final FortressServerManager serverManager = colonist.getFortressServerManager();
            final Optional<BlockPos> pos = serverManager.randomSurfacePos();
            if(pos.isPresent()) return false;
        }

        return !colonist.getTaskControl().hasTask() && isFarFromCenter();
    }

    private boolean isFarFromCenter() {
        final var serverManager = colonist.getFortressServerManager();
        final BlockPos fortressCenter = serverManager.getFortressCenter();
        return fortressCenter != null &&
                colonist.squaredDistanceTo(fortressCenter.getX(), fortressCenter.getY(), fortressCenter.getZ()) > Math.pow(serverManager.getHomeOuterRadius(), 2);
    }

    private boolean isNight() {
        return colonist.world.isNight();
    }


    @Override
    public void start() {
        super.start();
        moveToTheFire();
        this.colonist.setCurrentTaskDesc("Staying near campfire");
    }

    private void moveToTheFire() {
        final var randPos = colonist.getFortressServerManager().getRandomPositionAroundCampfire();
        if(randPos.isEmpty()) return;


        colonist.getMovementHelper().set(randPos.get().up(), Colonist.SLOW_MOVEMENT_SPEED);
        if(colonist.isSleeping()) {
            colonist.wakeUp();
        }
    }

    @Override
    public boolean shouldContinue() {
        return notInCombat() &&
                isNight() &&
                !colonist.getTaskControl().hasTask() &&
                !colonist.getMovementHelper().isStuck() &&
                (isFarFromCenter() || colonist.getMovementHelper().stillTryingToReachGoal());
    }

    @Override
    public void stop() {
        super.stop();
        colonist.getMovementHelper().reset();
    }
}
