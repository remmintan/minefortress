package org.minefortress.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.minefortress.entity.ai.goal.SelectTargetToAttackGoal;
import org.minefortress.entity.ai.goal.warrior.FollowLivingEntityGoal;
import org.minefortress.entity.ai.goal.warrior.MoveToBlockGoal;
import org.minefortress.entity.ai.goal.warrior.RangedAttackGoal;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IProfessional;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWarrior;

public class ArcherPawn extends TargetedPawn implements IWarrior, RangedAttackMob, IProfessional {

    public ArcherPawn(EntityType<? extends BasePawnEntity> entityType, World world) {
        super(entityType, world, false);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new RangedAttackGoal(this));
        this.goalSelector.add(2, new MoveToBlockGoal(this));
        this.goalSelector.add(2, new FollowLivingEntityGoal(this));
        this.goalSelector.add(9, new LookAtEntityGoal(this, LivingEntity.class, 4f));
        this.goalSelector.add(10, new LookAroundGoal(this));

        this.targetSelector.add(1, new SelectTargetToAttackGoal(this, this::canAttack));
    }

    private boolean canAttack(LivingEntity it) {
        return it.isAlive() && (it instanceof HostileEntity || it.equals(getAttackTarget()));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0d)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15d)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0d)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED)
                .add(EntityAttributes.GENERIC_LUCK);
    }

    @Override
    public double getAttackRange() {
        return this.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        final var itemStack = new ItemStack(Items.ARROW);
        final var persistentProjectileEntity = ProjectileUtil.createArrowProjectile(this, itemStack, pullProgress);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        persistentProjectileEntity.setVelocity(d, e + g * 0.20000000298023224, f, 1.6F, (float)(14 - this.getWorld().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.getWorld().spawnEntity(persistentProjectileEntity);
    }



    @Override
    public String getProfessionId() {
        return "archer1";
    }

    @Override
    public void resetProfession() {
        final var damageSources = this.getWorld().getDamageSources();
        this.damage(damageSources.outOfWorld(), 40f);
    }
}
