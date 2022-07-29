package org.minefortress.entity.ai.goal;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressServerManager;

import java.util.Optional;
import java.util.Random;

public class ReturnToFireGoal extends AbstractFortressGoal {

    private final Random random = new Random();

    public ReturnToFireGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        if(!notInCombat()) return false;
        if(!isNight()) {
            final FortressServerManager serverManager = colonist.getFortressServerManager();
            final Optional<BlockPos> pos = serverManager.randomSurfacePos((ServerWorld) colonist.world);
            if(pos.isPresent()) return false;
        }

        return !colonist.getTaskControl().hasTask() && isFarFromCenter();
    }

    private boolean isFarFromCenter() {
        final BlockPos fortressCenter = colonist.getFortressServerManager().getFortressCenter();
        return fortressCenter != null &&
                colonist.squaredDistanceTo(fortressCenter.getX(), fortressCenter.getY(), fortressCenter.getZ()) > Math.pow(getHomeOuterRadius(), 2);
    }

    private boolean isNight() {
        return colonist.world.isNight();
    }
    
    private int getHomeOuterRadius() {
        return Math.max(getColonistsCount(), 5) * 4 / 5;
    }

    @NotNull
    private Integer getColonistsCount() {
        return colonist.getFortressServerManager().getColonistsCount();
    }

    private int getHomeInnerRadius() {
        return Math.max(getColonistsCount(), 5) * 2 / 5;
    }

    @Override
    public void start() {
        super.start();
        moveToTheFire();
        this.colonist.setCurrentTaskDesc("Staying near campfire");
    }

    private void moveToTheFire() {
        final BlockPos fortressCenter = colonist.getFortressServerManager().getFortressCenter();

        final int x = random.nextInt(getHomeOuterRadius() - getHomeInnerRadius()) + getHomeInnerRadius() * (random.nextBoolean()?1:-1);
        final int z = random.nextInt(getHomeOuterRadius() - getHomeInnerRadius()) + getHomeInnerRadius() * (random.nextBoolean()?1:-1);

        BlockPos goal = new BlockPos(fortressCenter.getX() + x, fortressCenter.getY(), fortressCenter.getZ() + z);

        colonist.getMovementHelper().set(goal);

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
                isFarFromCenter();
    }

    @Override
    public void stop() {
        super.stop();
        colonist.getMovementHelper().reset();
    }
}
