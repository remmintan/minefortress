package org.minefortress.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.world.World;
import org.minefortress.entity.ai.goal.warrior.FollowLivingEntityGoal;
import org.minefortress.entity.ai.goal.warrior.MoveToBlockGoal;

public class ArcherPawn extends TargetedPawn {

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
