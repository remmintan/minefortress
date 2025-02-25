package org.minefortress.entity.ai.goal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IEatControl;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
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
                f.getManagersProvider()
                .map(IServerManagersProvider::getResourceManager)
                        .map(iServerResourceManager -> iServerResourceManager) :
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
                        .filter(EatGoal::isEatable)
                        .max(
                                Comparator.comparingDouble(stack ->
                                        {
                                            final var foodComponent = stack.getItem().getFoodComponent();
                                            //noinspection DataFlowIssue
                                            return foodComponent.getHunger() * foodComponent.getSaturationModifier() * 2.0f;
                                        }
                                )
                        )
                );
    }

    private static boolean isEatable(ItemStack stack) {
        if(stack.isEmpty() || !stack.getItem().isFood())
            return false;

        final var foodComponent = stack.getItem().getFoodComponent();
        final var statusEffects = foodComponent.getStatusEffects();
        if(statusEffects.isEmpty())
            return true;

        for (Pair<StatusEffectInstance, Float> statusEffect : statusEffects) {
            if(statusEffect.getFirst().getEffectType().getCategory() == StatusEffectCategory.HARMFUL)
                return false;
        }

        return true;
    }

}
