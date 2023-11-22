package org.minefortress.entity.fight;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NavigationTargetEntity extends Entity {

    private static final String TAG_AGE = "age";
    private static final int MAX_AGE = 5 * 30;

    private int age;

    public NavigationTargetEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if(age > MAX_AGE) {
            this.discard();
        }
    }

    @Override
    public float getYaw(float tickDelta) {
        return age % 45f;
    }

    @Override
    protected void initDataTracker() {}
    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if(nbt.contains(TAG_AGE)) {
            age = nbt.getInt(TAG_AGE);
        }
    }
    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt(TAG_AGE, age);
    }
    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {}

    @Override
    public boolean canMoveVoluntarily() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
