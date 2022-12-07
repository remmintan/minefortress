package org.minefortress.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.world.World;
import org.minefortress.entity.ai.goal.warrior.FollowLivingEntityGoal;
import org.minefortress.entity.ai.goal.warrior.MoveToBlockGoal;
import org.minefortress.entity.interfaces.IWarrior;

public class ArcherPawn extends TargetedPawn implements IWarrior {

    public ArcherPawn(EntityType<? extends BasePawnEntity> entityType, World world) {
        super(entityType, world, false);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(2, new MoveToBlockGoal(this));
        this.goalSelector.add(2, new FollowLivingEntityGoal(this));
        this.goalSelector.add(9, new LookAtEntityGoal(this, LivingEntity.class, 4f));
        this.goalSelector.add(10, new LookAroundGoal(this));
    }

    @Override
    public String getClothingId() {
        return "archer1";
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


    //    @Override
//    public String getProfessionId() {
//        return "archer1";
//    }
//
//    @Override
//    public void resetProfession() {
//        this.damage(DamageSource.OUT_OF_WORLD, 40f);
//    }
}
