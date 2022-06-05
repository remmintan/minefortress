package org.minefortress.entity.ai.professions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.minefortress.entity.Colonist;

public class WarriorDailyTask implements ProfessionDailyTask{

    private LivingEntity attackTarget;
    private int cooldown;

    @Override
    public boolean canStart(Colonist colonist) {
        return colonist.getFightControl().hasAttackTarget();
    }

    @Override
    public void start(Colonist colonist) {
        putCorrectSwordInHand(colonist);
        colonist.setCurrentTaskDesc("Fighting");
        this.cooldown = 0;
    }

    @Override
    public void tick(Colonist colonist) {
        if(!colonist.getFightControl().hasAttackTarget()) return;

        if (colonist.getActiveItem() == null || colonist.getActiveItem().getItem() != getCorrectItem(colonist)) {
            putCorrectSwordInHand(colonist);
        }

        attackTarget = colonist.getFightControl().getAttackTarget();
        final var distanceToAttackTarget = colonist.squaredDistanceTo(attackTarget);
        if(attackTarget != null && colonist.getNavigation().isIdle()) {
            if(distanceToAttackTarget > this.getSquaredMaxAttackDistance(attackTarget, colonist))
                colonist.getNavigation().startMovingTo(attackTarget, 1.75);
        }
        this.attack(distanceToAttackTarget, colonist);
        this.cooldown--;
        this.cooldown = Math.max(0, this.cooldown);
    }

    @Override
    public void stop(Colonist colonist) {
        colonist.getNavigation().stop();
        cooldown = 0;
        attackTarget = null;
    }

    @Override
    public boolean shouldContinue(Colonist colonist) {
        return colonist.getFightControl().hasAttackTarget();
    }

    private void putCorrectSwordInHand(Colonist colonist) {
        colonist.putItemInHand(getCorrectItem(colonist));
    }

    private Item getCorrectItem(Colonist colonist) {
        if(colonist.getProfessionId().equals("warrior1"))
            return Items.STONE_SWORD;
        else if(colonist.getProfessionId().equals("warrior2"))
            return Items.IRON_SWORD;
        else
            return Items.WOODEN_SWORD;
    }


    private void attack(double squaredDistance, Colonist colonist) {
        double d = this.getSquaredMaxAttackDistance(this.attackTarget, colonist);
        if (squaredDistance <= d && this.cooldown <= 0) {
            this.resetCooldown();
            colonist.swingHand(Hand.MAIN_HAND);
            colonist.tryAttack(this.attackTarget);
        }
    }

    private double getSquaredMaxAttackDistance(LivingEntity entity, Colonist colonist) {
        return colonist.getWidth() * 2.0f * (colonist.getWidth() * 2.0f) + entity.getWidth();
    }

    private void resetCooldown() {
        this.cooldown = 10;
    }

}
