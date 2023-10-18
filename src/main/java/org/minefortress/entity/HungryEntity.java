package org.minefortress.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.minefortress.entity.ai.controls.EatControl;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IEatControl;
import org.minefortress.entity.ai.goal.EatGoal;
import org.minefortress.entity.colonist.FakeHungerManager;
import org.minefortress.entity.colonist.FortressHungerManager;
import org.minefortress.entity.colonist.IFortressHungerManager;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IHungerAwareEntity;

import java.util.Optional;

public abstract class HungryEntity extends BaritonableEntity implements IHungerAwareEntity {

    private static final TrackedData<Integer> CURRENT_FOOD_LEVEL = DataTracker.registerData(HungryEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final String HUNGER_MANAGER_NBT_KEY = "hunger";

    private final IFortressHungerManager hungerManager;
    private final IEatControl eatControl;

    protected HungryEntity(EntityType<? extends PathAwareEntity> entityType, World world, boolean enableHunger) {
        super(entityType, world);
        hungerManager = enableHunger ? new FortressHungerManager() : new FakeHungerManager();

        if(world instanceof ServerWorld) {
            eatControl = new EatControl(this);
        } else {
            eatControl = null;
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(CURRENT_FOOD_LEVEL, HungerConstants.FULL_FOOD_LEVEL);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        if(!(hungerManager instanceof FakeHungerManager)) {
            this.goalSelector.add(7, new EatGoal(this));
        }
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if(eatControl != null) eatControl.tick();
        hungerManager.update(this);
        if(this.getCurrentFoodLevel() != getHungerManager().getFoodLevel()) {
            sendHungerMessage();
            this.updateCurrentFoodLevel();
        }
    }

    @Override
    public float getHealth() {
        return super.getHealth();
    }

    @Override
    public final HungerManager getHungerManager() {
        return hungerManager.toHungerManager();
    }

    @Override
    public final int getCurrentFoodLevel() {
        return this.dataTracker.get(CURRENT_FOOD_LEVEL);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        final var nbtCompound = new NbtCompound();
        getHungerManager().writeNbt(nbtCompound);
        nbt.put(HUNGER_MANAGER_NBT_KEY, nbtCompound);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        final var nbtCompound = nbt.getCompound(HUNGER_MANAGER_NBT_KEY);
        getHungerManager().readNbt(nbtCompound);
    }

    @Override
    public final ItemStack eatFood(World world, ItemStack stack) {
        this.getHungerManager().eat(stack.getItem(), stack);
        return super.eatFood(world, stack);
    }

    private void sendHungerMessage() {
        if(this instanceof IFortressAwareEntity fae && this instanceof NamedPawnEntity npe) {
            final HungerManager hungerManager = getHungerManager();
            final var pawnName = npe.getName().getString();
            if(hungerManager.prevFoodLevel > 0 && hungerManager.getFoodLevel() <= 0) {
                fae.sendMessageToMasterPlayer(pawnName + " is starving! Do something!");
            } else if(hungerManager.prevFoodLevel >= 5 && hungerManager.foodLevel < 5) {
                fae.sendMessageToMasterPlayer(pawnName + " is very hungry! Bring some food to the village!");
            } else if(hungerManager.prevFoodLevel >= 10 && hungerManager.foodLevel < 10) {
                fae.sendMessageToMasterPlayer(pawnName + " is hungry. It's time to eat something!");
            }
        }
    }

    @Override
    public Optional<IEatControl> getEatControl() {
        return Optional.ofNullable(eatControl);
    }

    private void updateCurrentFoodLevel() {
        this.dataTracker.set(CURRENT_FOOD_LEVEL, this.hungerManager.toHungerManager().getFoodLevel());
    }

    @Override
    public ItemStack getActiveItem() {
        return super.getActiveItem();
    }

    @Override
    public int getItemUseTimeLeft() {
        return super.getItemUseTimeLeft();
    }

    @Override
    public boolean isUsingItem() {
        return super.isUsingItem();
    }

    @Override
    public void setCurrentHand(Hand hand) {
        super.setCurrentHand(hand);
    }

    @Override
    public ItemStack getStackInHand(Hand hand) {
        return super.getStackInHand(hand);
    }

    @Override
    public void setStackInHand(Hand hand, ItemStack stack) {
        super.setStackInHand(hand, stack);
    }
}
