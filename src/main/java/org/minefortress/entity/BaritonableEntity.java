package org.minefortress.entity;

import baritone.api.minefortress.IMinefortressEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BucketItem;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.minefortress.entity.ai.MineFortressInventory;

public abstract class BaritonableEntity extends PathAwareEntity implements IMinefortressEntity {

    private static final TrackedData<Integer> SELECTED_SLOT = DataTracker.registerData(BaritonableEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final Inventory fakeInventoryWithPlanks = new MineFortressInventory(true);
    private final Inventory fakeInventoryWithoutPlanks = new MineFortressInventory(false);

    protected BaritonableEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SELECTED_SLOT, 0);
    }

    @Override
    public final Inventory getInventory() {
        return this instanceof WarriorPawn ? fakeInventoryWithoutPlanks : fakeInventoryWithPlanks;
    }

    @Override
    public final void selectSlot(int i) {
        this.dataTracker.set(SELECTED_SLOT, i);
        this.setStackInHand(Hand.MAIN_HAND, this.getInventory().getStack(i));
    }

    @Override
    public final int getSelectedSlot() {
        return this.dataTracker.get(SELECTED_SLOT);
    }

    @Override
    public final Fluid getBucketFluid(BucketItem bucketItem) {
        return bucketItem.fluid;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if(data.equals(SELECTED_SLOT)) {
            final var selectedSlot = this.dataTracker.get(SELECTED_SLOT);
            this.setStackInHand(Hand.MAIN_HAND, this.getInventory().getStack(selectedSlot));
        }

        super.onTrackedDataSet(data);
    }
}
