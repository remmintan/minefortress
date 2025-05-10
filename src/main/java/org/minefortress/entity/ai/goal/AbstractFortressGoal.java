package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IEatControl;
import org.minefortress.entity.Colonist;

import java.util.EnumSet;

public abstract class AbstractFortressGoal extends Goal {

    protected final Colonist colonist;

    protected AbstractFortressGoal(Colonist colonist) {
        this.colonist = colonist;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK,Goal.Control.JUMP));
        World level = this.colonist.getWorld();
        if (!(level instanceof ServerWorld)) {
            throw new IllegalStateException("AI should run on the server entities!");
        }
    }

    protected String getColonistName() {
        return colonist.getName().getString();
    }

    protected boolean isHungry() {
        return colonist.getEatControl().map(IEatControl::isHungry).orElse(false);
    }



}
