package org.minefortress.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.HungryEntity;
import org.minefortress.entity.ai.controls.EatControl;
import org.minefortress.entity.interfaces.IFortressAwareEntity;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.resources.server.ServerResourceManager;

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

    private Optional<ServerResourceManager> getResourceManager() {
        return entity instanceof IFortressAwareEntity f ?
                f.getFortressServerManager()
                .map(FortressServerManager::getResourceManager)
                .map(ServerResourceManager.class::cast) :
                Optional.empty();
    }

    @NotNull
    private EatControl getEatControl() {
        return entity.getEatControl().orElseThrow();
    }

    @NotNull
    private Optional<ItemStack> getEatableItem() {
        return getResourceManager()
                .flatMap(it -> it
                        .getAllItems()
                        .stream()
                        .filter(stack -> !stack.isEmpty() && stack.getItem().isFood())
                        .min(
                                Comparator.comparingDouble(stack ->
                                        Optional.ofNullable(stack.getItem().getFoodComponent())
                                                .map(item -> item.getHunger() * item.getSaturationModifier() * 2.0f)
                                                .orElse(0f)
                                )
                        )
                );
    }
}
