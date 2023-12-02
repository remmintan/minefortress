package net.remmintan.mods.minefortress.core.interfaces.combat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManager;

public interface IServerFightManager extends IServerManager {
    void spawnDebugWarriors(int num, ServerPlayerEntity player);
    void setCurrentTarget(BlockPos pos, ServerWorld world);
    void attractWarriorsToCampfire();
    void sync();

}
