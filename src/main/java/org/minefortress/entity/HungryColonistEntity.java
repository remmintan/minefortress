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
import net.minecraft.world.World;
import org.minefortress.entity.ai.controls.EatControl;
import org.minefortress.entity.colonist.FakeHungerManager;
import org.minefortress.entity.colonist.FortressHungerManager;
import org.minefortress.entity.colonist.IFortressHungerManager;

public abstract class HungryColonistEntity extends BaritonableEntity implements IHungerAwareEntity{

    private static final TrackedData<Integer> CURRENT_FOOD_LEVEL = DataTracker.registerData(HungryColonistEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final String HUNGER_MANAGER_NBT_KEY = "hunger";

    private final IFortressHungerManager fortHungMan;
    private final EatControl eatControl;

    protected HungryColonistEntity(EntityType<? extends PathAwareEntity> entityType, World world, boolean enableHunger) {
        super(entityType, world);
        fortHungMan = enableHunger ? new FortressHungerManager() : new FakeHungerManager();

        if(world instanceof ServerWorld) {
            eatControl = new EatControl(this);
        } else {
            eatControl = null;
        }

        this.dataTracker.startTracking(CURRENT_FOOD_LEVEL, HungerConstants.FULL_FOOD_LEVEL);
    }

    @Override
    public void tick() {
        super.tick();
        if(eatControl != null) eatControl.tick();
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        fortHungMan.update(this);
        if(this.getCurrentFoodLevel() != getHungerManager().getFoodLevel()) {
            sendHungerMessage();
            this.updateCurrentFoodLevel();
        }
    }

    @Override
    public final HungerManager getHungerManager() {
        return fortHungMan.toHungerManager();
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
        if(this instanceof IFortressAwareEntity fae) {
            final HungerManager hungerManager = getHungerManager();
            if(hungerManager.prevFoodLevel > 0 && hungerManager.getFoodLevel() <= 0) {
                fae.sendMessageToMasterPlayer(getName().asString() + "is starving! Do something!");
            } else if(hungerManager.prevFoodLevel >= 5 && hungerManager.foodLevel < 5) {
                fae.sendMessageToMasterPlayer(getName().asString() + " is very hungry! Bring some food to the village!");
            } else if(hungerManager.prevFoodLevel >= 10 && hungerManager.foodLevel < 10) {
                fae.sendMessageToMasterPlayer(getName().asString() + " is hungry. It's time to eat something!");
            }
        }
    }

    private void updateCurrentFoodLevel() {
        this.dataTracker.set(CURRENT_FOOD_LEVEL, this.fortHungMan.toHungerManager().getFoodLevel());
    }
}
