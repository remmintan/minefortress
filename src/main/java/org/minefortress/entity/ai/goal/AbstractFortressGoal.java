package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IEatControl;
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerFoodManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.minefortress.entity.Colonist;
import org.minefortress.events.HungryPawnStartsWorkingCallback;

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

    protected boolean wantAndCanEatSomeFood() {
        final var hungry = colonist.getEatControl().map(IEatControl::isHungry).orElse(false);
        final var hasSomeFoodToIt = ServerModUtils.getManagersProvider(colonist.getServer(), colonist.getFortressPos())
                .map(IServerManagersProvider::getFoodManager)
                .map(IServerFoodManager::hasFood)
                .orElse(false);

        if (hungry && !hasSomeFoodToIt) {
            HungryPawnStartsWorkingCallback.EVENT.invoker().hungryPawnStartsWorking(colonist, colonist.getCurrentFoodLevel());
        }

        return hungry && hasSomeFoodToIt;
    }



}
