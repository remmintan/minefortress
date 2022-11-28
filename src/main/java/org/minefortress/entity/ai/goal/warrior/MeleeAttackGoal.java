package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.interfaces.IProfessional;

import java.util.Optional;

public class MeleeAttackGoal extends Goal {

    private final BasePawnEntity pawn;

    public MeleeAttackGoal(BasePawnEntity pawn) {
        this.pawn = pawn;
    }

    @Override
    public boolean canStart() {
        return getTarget().map(LivingEntity::isAlive).orElse(false);
    }

    @Override
    public void start() {
        super.start();
        this.pawn.putItemInHand(getCorrectItem());
    }

    private Item getCorrectItem() {
        if(pawn instanceof IProfessional professional) {
            return switch (professional.getProfessionId()) {
                case "warrior1" -> Items.STONE_SWORD;
                case "warrior2" -> Items.IRON_SWORD;
                default -> Items.AIR;
            };
        }
        return Items.AIR;
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
