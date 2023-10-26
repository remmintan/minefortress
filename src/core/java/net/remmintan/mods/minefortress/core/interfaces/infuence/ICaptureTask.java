package net.remmintan.mods.minefortress.core.interfaces.infuence;

import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public interface ICaptureTask {
    UUID taskId();

    BlockPos pos();
}
