package net.remmintan.mods.minefortress.core.interfaces.infuence;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorderStage;

public interface IInfluencePosStateHolder {
    void syncNewPos(BlockPos newPos);

    void setCorrect(WorldBorderStage state);

    WorldBorderStage getWorldBorderStage();

    void reset();
}
