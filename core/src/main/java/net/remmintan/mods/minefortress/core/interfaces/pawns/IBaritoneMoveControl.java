package net.remmintan.mods.minefortress.core.interfaces.pawns;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public interface IBaritoneMoveControl {
    void moveTo(@NotNull BlockPos pos);

    void moveTo(@NotNull LivingEntity entity);

    void reset();

    boolean isStuck();
}
