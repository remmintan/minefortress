package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IEatControl;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.HungryEntity;

import java.util.Optional;

public class EatGoal extends Goal {
    private final HungryEntity entity;

    public EatGoal(HungryEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean canStart() {
        return getEatControl().isHungry() && getResourceManager().flatMap(IServerResourceManager::getEatableItem).isPresent();
    }

    @Override
    public void start() {
        getResourceManager().ifPresent(it ->
                it.getEatableItem().ifPresent(st -> {
                it.increaseItemAmount(st.getItem(), -1);
                getEatControl().eatFood(st.getItem());
            })
        );
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && getEatControl().isEating();
    }

    private Optional<IServerResourceManager> getResourceManager() {
        return entity instanceof IFortressAwareEntity fortressEntity ?
                ServerModUtils.getManagersProvider(fortressEntity)
                        .map(IServerManagersProvider::getResourceManager) :
                Optional.empty();
    }

    @NotNull
    private IEatControl getEatControl() {
        return entity.getEatControl().orElseThrow();
    }



}
