package org.minefortress.entity.ai.goal;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;

import java.util.Optional;

public class ColonistEatGoal extends AbstractFortressGoal {

    private BlockPos goal;
    private Item foodInHand;

    public ColonistEatGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        return colonist.getEatControl().isHungryEnough() && getRandomPositionAroundCampfire().isPresent() && colonist.getEatControl().hasEatableItem() && notInCombat();
    }

    @Override
    public void start() {
        final var randPos = getRandomPositionAroundCampfire();
        if(randPos.isEmpty())return;

        this.goal = randPos.get().up();
        colonist.getMovementHelper().set(goal, Colonist.FAST_MOVEMENT_SPEED);
        this.colonist.setCurrentTaskDesc("Looking for food");
    }

    private Optional<BlockPos> getRandomPositionAroundCampfire() {
        return colonist.getFortressServerManager().orElseThrow().getRandomPositionAroundCampfire();
    }

    @Override
    public void tick() {
        final MovementHelper movementHelper = colonist.getMovementHelper();
        if(movementHelper.hasReachedWorkGoal()) {
            if(!colonist.getEatControl().isEating())
                colonist.getEatControl().putFoodInHand();
        }

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

}
