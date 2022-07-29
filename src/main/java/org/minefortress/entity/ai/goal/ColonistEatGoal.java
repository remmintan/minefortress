package org.minefortress.entity.ai.goal;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;

public class ColonistEatGoal extends AbstractFortressGoal {

    private BlockPos goal;
    private Item foodInHand;

    public ColonistEatGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        return colonist.getEatControl().isHungryEnough() && colonist.getFortressServerManager().getFortressCenter() != null && colonist.getEatControl().hasEatableItem() && notInCombat();
    }

    @Override
    public void start() {
        super.start();
        final BlockPos fortressCenter = colonist.getFortressServerManager().getFortressCenter();

        final var random = colonist.world.random;
        final int x = random.nextInt(getHomeOuterRadius() - getHomeInnerRadius()) + getHomeInnerRadius() * (random.nextBoolean()?1:-1);
        final int z = random.nextInt(getHomeOuterRadius() - getHomeInnerRadius()) + getHomeInnerRadius() * (random.nextBoolean()?1:-1);

        this.goal = new BlockPos(fortressCenter.getX() + x, fortressCenter.getY(), fortressCenter.getZ() + z);
        colonist.getMovementHelper().set(goal);
        this.colonist.setCurrentTaskDesc("Looking for food");
    }

    @Override
    public void tick() {
        final MovementHelper movementHelper = colonist.getMovementHelper();
        if(movementHelper.hasReachedWorkGoal()) {
            if(!colonist.getEatControl().isEating())
                colonist.getEatControl().putFoodInHand();
        }
        movementHelper.tick();

        if(!movementHelper.hasReachedWorkGoal() && movementHelper.isStuck())
            colonist.teleport(this.goal.getX(), this.goal.getY(), this.goal.getZ());
    }

    @Override
    public boolean shouldContinue() {
        return notInCombat() && colonist.getEatControl().isHungryEnough() && (colonist.getEatControl().hasEatableItem() || this.foodInHand != null || hasntReachedTheWorkGoal());
    }

    private boolean hasntReachedTheWorkGoal() {
        return this.goal != null && !colonist.getMovementHelper().hasReachedWorkGoal();
    }

    @Override
    public boolean canStop() {
        return !colonist.getEatControl().hasEatableItem() || isScared();
    }

    @Override
    public void stop() {
        this.foodInHand = null;
        this.goal = null;
        colonist.getMovementHelper().reset();
        colonist.putItemInHand(this.foodInHand);
        colonist.getEatControl().reset();
    }

    private int getHomeOuterRadius() {
        return 8;
    }

    private int getHomeInnerRadius() {
        return 3;
    }

}
