package net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls;

import net.minecraft.item.Item;

public interface IEatControl {
    boolean isHungry();

    void tick();

    void reset();

    void eatFood(Item food);

    boolean isEating();
}
