package net.remmintan.mods.minefortress.core.interfaces.combat;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;

public interface IClientFightManager {
    IClientFightSelectionManager getSelectionManager();

    void setTarget(HitResult hitResult);

    void setTarget(Entity entity);
    void sync(int warriorsCount);
    int getWarriorCount();
    void attractWarriorsToCampfire();
}
