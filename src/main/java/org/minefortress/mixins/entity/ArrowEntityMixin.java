package org.minefortress.mixins.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PersistentProjectileEntity.class)
public abstract class ArrowEntityMixin extends ProjectileEntity {

    protected ArrowEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }


    @SuppressWarnings("ConstantConditions") // it's a mixin, so if statement will work fine
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;getEntityCollision(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/hit/EntityHitResult;"))
    public EntityHitResult tick(PersistentProjectileEntity instance, Vec3d currentPosition, Vec3d nextPosition) {
        final var collision = instance.getEntityCollision(currentPosition, nextPosition);
        if(collision != null && ((Object) this instanceof ArrowEntity)) {
            final var collidedWith = collision.getEntity();
            final var owner = this.getOwner();
            if(
               owner instanceof IFortressAwareEntity pawn1 &&
               collidedWith instanceof IFortressAwareEntity pawn2 &&
                       pawn1.getFortressPos().equals(pawn2.getFortressPos())
            ) {
                return null;
            }
        }
        return collision;
    }
}
