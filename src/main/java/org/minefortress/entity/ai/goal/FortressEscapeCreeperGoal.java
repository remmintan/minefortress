package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.CreeperEntity;
import org.minefortress.entity.Colonist;

public class FortressEscapeCreeperGoal extends FleeEntityGoal {

    private final Colonist colonist;

    public FortressEscapeCreeperGoal(Colonist colonist) {
        super(colonist, CreeperEntity.class, 4, 1.25, 1.75);
        this.colonist = colonist;
    }

    @Override
    public boolean canStart() {
        return super.canStart() && !colonist.getFightControl().isForcedToAttackCreeper();
    }
}
