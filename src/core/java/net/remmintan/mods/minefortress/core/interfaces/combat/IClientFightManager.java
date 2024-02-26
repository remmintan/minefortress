package net.remmintan.mods.minefortress.core.interfaces.combat;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;

public interface IClientFightManager {

    void setTarget(HitResult hitResult, ITargetedSelectionManager targetedSelectionManager);

    void setTarget(Entity entity, ITargetedSelectionManager targetedSelectionManager);
    void sync(int warriorsCount);
    int getWarriorCount();
    void attractWarriorsToCampfire();
}
