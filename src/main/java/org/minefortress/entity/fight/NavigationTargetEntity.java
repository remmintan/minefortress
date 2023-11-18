package org.minefortress.entity.fight;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NavigationTargetEntity extends Entity {
    public NavigationTargetEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker() {}
    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {}
    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {}
    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {}

    @Override
    public boolean canMoveVoluntarily() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
