package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.resources.server.ServerResourceManager;

import java.util.EnumSet;
import java.util.Optional;

public class ColonistEatGoal extends AbstractFortressGoal {

    private BlockPos goal;
    private Item foodInHand;

    public ColonistEatGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        return isHungryEnough() && colonist.getFortressCenter() != null && this.hasEatableItem() && notInCombat();
    }

    private boolean isHungryEnough() {
        return colonist.getCurrentFoodLevel() < 12 || (colonist.getHealth() <= 10 && colonist.getCurrentFoodLevel() < 20);
    }

    @Override
    public void start() {
        super.start();
        final BlockPos fortressCenter = colonist.getFortressCenter().toImmutable();

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
            if(this.foodInHand != null && !colonist.getActiveItem().isEmpty() && colonist.getItemUseTimeLeft() <= 0) {
                this.foodInHand = null;
                colonist.putItemInHand(this.foodInHand);
            } else if(this.foodInHand != null) {
                this.colonist.setCurrentTaskDesc("Eating...");
                colonist.putItemInHand(this.foodInHand);
                if(!colonist.isUsingItem()) {
                    colonist.setCurrentHand(Hand.MAIN_HAND);
                }
            } else {
                putFoodInHand();
            }
        }
        movementHelper.tick();

        if(!movementHelper.hasReachedWorkGoal() && movementHelper.isCantFindPath())
            colonist.teleport(this.goal.getX(), this.goal.getY(), this.goal.getZ());
    }

    @Override
    public boolean shouldContinue() {
        return notInCombat() && isHungryEnough() && (hasEatableItem() || this.foodInHand != null || hasntReachedTheWorkGoal());
    }

    private boolean hasntReachedTheWorkGoal() {
        return this.goal != null && !colonist.getMovementHelper().hasReachedWorkGoal();
    }

    @Override
    public boolean canStop() {
        return hasEatableItem();
    }

    @Override
    public void stop() {
        this.foodInHand = null;
        this.goal = null;
        this.colonist.getNavigation().stop();
        colonist.putItemInHand(this.foodInHand);
    }

    private void putFoodInHand() {
        this.getEatableItem().ifPresent(item -> {
            getServerResourceManager()
                    .ifPresent(man -> man.increaseItemAmount(item.getItem(), -1));
            this.foodInHand = item.getItem();
        });
    }

    private Optional<ItemStack> getEatableItem() {
        return getServerResourceManager()
                .map(ServerResourceManager::getAllItems)
                .flatMap(it -> it.stream().filter(this::isEatableItem).findFirst());
    }

    private boolean hasEatableItem() {
        return getServerResourceManager()
                .map(ServerResourceManager::getAllItems)
                .map(it -> it.stream().anyMatch(this::isEatableItem))
                .orElse(false);
    }

    private boolean isEatableItem(ItemStack st) {
        return !st.isEmpty() && st.getItem().isFood();
    }

    private Optional<ServerResourceManager> getServerResourceManager() {
        return colonist.getFortressServerManager().map(FortressServerManager::getServerResourceManager);
    }

    private int getHomeOuterRadius() {
        return 8;
    }

    private int getHomeInnerRadius() {
        return 3;
    }

}
