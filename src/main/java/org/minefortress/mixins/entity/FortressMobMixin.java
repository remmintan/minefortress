package org.minefortress.mixins.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.*;
import net.minecraft.world.World;
import org.minefortress.entity.Colonist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({
        AbstractSkeletonEntity.class,
        ZombieEntity.class,
        BlazeEntity.class,
        CreeperEntity.class,
        EndermiteEntity.class,
        IllagerEntity.class,
        IllusionerEntity.class,
        PatrolEntity.class,
        PillagerEntity.class,
        RavagerEntity.class,
        SpiderEntity.class,
        VexEntity.class,
        VindicatorEntity.class,
        WitchEntity.class
})
public abstract class FortressMobMixin extends HostileEntity {

    protected FortressMobMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method="initGoals", at=@At("TAIL"))
    protected void initGoals(CallbackInfo ci) {
        super.targetSelector.add(2, new ActiveTargetGoal<>(this, Colonist.class, true));
    }

}
