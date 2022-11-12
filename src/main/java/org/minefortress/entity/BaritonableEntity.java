package org.minefortress.entity;

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

public abstract class BaritonableEntity extends PathAwareEntity implements IBaritonableEntity {

    private static final TrackedData<Integer> SELECTED_SLOT = DataTracker.registerData(BaritonableEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final Inventory fakeInventory;

    protected BaritonableEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);

        this.fakeInventory = new MineFortressInventory();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SELECTED_SLOT, 0);
    }

    @Override
    public final Inventory getInventory() {
        return fakeInventory;
    }

    @Override
    public final void selectSlot(int i) {
        this.dataTracker.set(SELECTED_SLOT, i);
    }

    @Override
    public final int getSelectedSlot() {
        return this.dataTracker.get(SELECTED_SLOT);
    }

    @Override
    public final Fluid getBucketFluid(BucketItem bucketItem) {
        return bucketItem.fluid;
    }

    public float getReachRange() {
        return 2.5f;
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
