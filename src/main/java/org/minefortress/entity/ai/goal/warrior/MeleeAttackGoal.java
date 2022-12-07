package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.interfaces.IProfessional;

public class MeleeAttackGoal extends AttackGoal {


    public MeleeAttackGoal(BasePawnEntity pawn) {
        super(pawn);
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

}
