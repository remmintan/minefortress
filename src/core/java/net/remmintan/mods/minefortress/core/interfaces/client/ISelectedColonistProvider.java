package net.remmintan.mods.minefortress.core.interfaces.client;

import net.minecraft.entity.LivingEntity;

public interface ISelectedColonistProvider {

    boolean isSelectingColonist();

    LivingEntity getSelectedPawn();

}
