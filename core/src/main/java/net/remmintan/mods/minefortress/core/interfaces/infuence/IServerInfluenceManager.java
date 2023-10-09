package net.remmintan.mods.minefortress.core.interfaces.infuence;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface IServerInfluenceManager {

    void addCapturePosition(UUID taskId, BlockPos pos, ServerPlayerEntity player);

}
