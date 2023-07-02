package org.minefortress.mixins.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.world.World;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.interfaces.FortressSlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SlimeEntity.class)
public abstract class FortressSlimeEntityMixin extends MobEntity implements FortressSlimeEntity {

    protected FortressSlimeEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    protected abstract boolean canAttack();

    @Shadow
    protected abstract void damage(LivingEntity entity);

    @Override
    public void touchPawn(BasePawnEntity colonist) {
        if(canAttack()) {
            damage(colonist);
        }
    }
}
