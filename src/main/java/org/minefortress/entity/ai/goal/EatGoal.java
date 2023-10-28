package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IEatControl;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerFortressManager;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.HungryEntity;

import java.util.Comparator;
import java.util.Optional;

public class EatGoal extends Goal {
    private final HungryEntity entity;

    public EatGoal(HungryEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean canStart() {
        return getEatControl().isHungry() && getEatableItem().isPresent();
    }

    @Override
    public void start() {
        getResourceManager().ifPresent(it ->
            getEatableItem().ifPresent(st -> {
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
        return entity instanceof IFortressAwareEntity f ?
                f.getServerFortressManager()
                .map(IServerFortressManager::getResourceManager)
                .map(IServerResourceManager.class::cast) :
                Optional.empty();
    }

    @NotNull
    private IEatControl getEatControl() {
        return entity.getEatControl().orElseThrow();
    }

    @NotNull
    private Optional<ItemStack> getEatableItem() {
        return getResourceManager()
                .flatMap(it -> it
                        .getAllItems()
                        .stream()
                        .filter(stack -> !stack.isEmpty() && stack.getItem().isFood())
                        .max(
                                Comparator.comparingDouble(stack ->
                                        Optional.ofNullable(stack.getItem().getFoodComponent())
                                                .map(item -> item.getHunger() * item.getSaturationModifier() * 2.0f)
                                                .orElse(0f)
                                )
                        )
                );
    }
}
