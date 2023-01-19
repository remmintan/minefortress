package org.minefortress.areas;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vector4f;

import java.util.List;
import java.util.UUID;

public record AutomationAreaInfo(
        List<BlockPos> area,
        Vector4f color,
        String name,
        UUID id
) {}
