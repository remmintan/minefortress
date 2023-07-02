package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.interfaces.IProfessional;

public class MeleeAttackGoal extends AttackGoal {


    private final BasePawnEntity pawn;
    private int cooldown = 0;

    public MeleeAttackGoal(BasePawnEntity pawn) {
        super(pawn);
        this.pawn = pawn;
    }

    @Override
    public void start() {
        super.start();
        this.pawn.putItemInHand(getCorrectItem());
        getTarget().ifPresent(it -> this.pawn.getLookControl().lookAt(it));
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
            if(cooldown <= 0) {
                pawn.swingHand(Hand.MAIN_HAND);
                pawn.tryAttack(it);
                cooldown = this.getTickCount(pawn.getAttackCooldown());
            }
        });

        if(cooldown > 0) cooldown--;
    }

}
