package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.minefortress.entity.interfaces.IItemUsingEntity;
import org.minefortress.entity.interfaces.IWarriorPawn;

import java.util.Optional;

public class MeleeAttackGoal extends Goal {

    private final IWarriorPawn pawn;

    public MeleeAttackGoal(IWarriorPawn pawn) {
        this.pawn = pawn;
    }

    @Override
    public boolean canStart() {
        return getTarget().map(LivingEntity::isAlive).orElse(false);
    }

    @Override
    public void start() {
        super.start();
        if(this.pawn instanceof IItemUsingEntity be) {
            be.putItemInHand(Items.IRON_SWORD);
        }
    }

    @Override
    public void tick() {
        getTarget().ifPresent(it -> {
            pawn.swingHand(Hand.MAIN_HAND);
            pawn.tryAttack(it);
        });
    }

    @Override
    public boolean canStop() {
        return true;
    }

    private Optional<LivingEntity> getTarget() {
        return Optional.ofNullable(pawn.getTarget());
    }

}
