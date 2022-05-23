package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.MovementHelper;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.resources.server.ServerResourceManager;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ColonistEatGoal extends Goal {

    private static final List<Item> FOOD_ITEMS = Arrays.asList(
            Items.APPLE,
            Items.BAKED_POTATO,
            Items.BEETROOT,
            Items.BEETROOT_SOUP,
            Items.BREAD,
            Items.CAKE,
            Items.CARROT,
            Items.CHORUS_FRUIT,
            Items.COCOA_BEANS,
            Items.COOKED_CHICKEN,
            Items.COOKED_COD,
            Items.COOKED_MUTTON,
            Items.COOKED_PORKCHOP,
            Items.COOKED_RABBIT,
            Items.COOKED_SALMON,
            Items.COOKIE,
            Items.DRIED_KELP,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.EGG,
            Items.HONEY_BOTTLE,
            Items.MELON,
            Items.MELON_SLICE,
            Items.MUSHROOM_STEW,
            Items.BROWN_MUSHROOM,
            Items.RED_MUSHROOM,
            Items.PUFFERFISH,
            Items.PUMPKIN_PIE,
            Items.RABBIT_STEW,
            Items.BEEF,
            Items.CHICKEN,
            Items.COD,
            Items.MUTTON,
            Items.PORKCHOP,
            Items.RABBIT,
            Items.SALMON,
            Items.SUSPICIOUS_STEW,
            Items.SWEET_BERRIES,
            Items.TROPICAL_FISH,
            Items.WHEAT
    );

    private final Colonist colonist;
    private BlockPos goal;
    private Item foodInHand;

    public ColonistEatGoal(Colonist colonist) {
        this.colonist = colonist;
        World level = this.colonist.world;
        if (!(level instanceof ServerWorld)) {
            throw new IllegalStateException("AI should run on the server entities!");
        }

        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        return colonist.getCurrentFoodLevel() < 10 && colonist.getFortressCenter() != null;
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
            if(this.foodInHand != null) {
                colonist.eatFood(colonist.world, new ItemStack(this.foodInHand));
                this.foodInHand = null;
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
        return colonist.getCurrentFoodLevel() < 18 && (colonist.getFoodSaturation() > 0 || hasEatableItem() || this.foodInHand != null);
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
        return !st.isEmpty() && FOOD_ITEMS.contains(st.getItem());
    }

    private Optional<ServerResourceManager> getServerResourceManager() {
        return colonist.getFortressServerManager().map(FortressServerManager::getServerResourceManager);
    }

    private int getHomeOuterRadius() {
        return Math.max(getColonistsCount(), 5) * 4 / 5;
    }


    private int getColonistsCount() {
        return colonist.getFortressServerManager().map(FortressServerManager::getColonistsCount).orElse(5);
    }

    private int getHomeInnerRadius() {
        return Math.max(getColonistsCount(), 5) * 2 / 5;
    }

}
