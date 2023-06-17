package org.minefortress.entity.ai.goal;

import org.minefortress.entity.Colonist;
import org.minefortress.entity.ai.goal.warrior.MeleeAttackGoal;

import java.util.Optional;

public class ColonistMeleeAttackGoal extends MeleeAttackGoal {

    private final Colonist colonist;
    public ColonistMeleeAttackGoal(Colonist pawn) {
        super(pawn);
        this.colonist = pawn;
    }

    @Override
    public boolean canStart() {
        return super.canStart() &&
                Optional.ofNullable(colonist.getTarget())
                        .map(it -> it.squaredDistanceTo(colonist))
                        .orElse(Double.MAX_VALUE)
                     <= 4f;
    }
}
