package net.remmintan.mods.minefortress.core.interfaces.combat;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface IServerFightManager {

    void setCurrentTarget(BlockPos pos, ServerWorld world);

}
