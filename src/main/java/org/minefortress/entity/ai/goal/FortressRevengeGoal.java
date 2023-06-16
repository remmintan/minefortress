package org.minefortress.entity.ai.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import org.minefortress.entity.Colonist;

import java.util.List;

public class FortressRevengeGoal extends RevengeGoal {
    public FortressRevengeGoal(PathAwareEntity mob, Class<?>... noRevengeTypes) {
        super(mob, noRevengeTypes);
    }

    @Override
    protected void callSameTypeForRevenge() {
        double d = this.getFollowRange();
        Box box = Box.from(this.mob.getPos()).expand(d, 10.0, d);
        List<Colonist> list = this.mob.world.getEntitiesByClass(Colonist.class, box, EntityPredicates.EXCEPT_SPECTATOR.and(this::fromTheSameFortress));
        for (Colonist colonist : list) {
            if (this.mob == colonist || colonist.getTarget() != null) continue;
            this.setMobEntityTarget(colonist, this.mob.getAttacker());
        }
    }

    private boolean fromTheSameFortress(Entity entity) {
        final var thisColonist = (Colonist) this.mob;
        if(entity instanceof Colonist colonist) {
            return colonist.getMasterId().equals(thisColonist.getMasterId());
        }
        return false;
    }
}
