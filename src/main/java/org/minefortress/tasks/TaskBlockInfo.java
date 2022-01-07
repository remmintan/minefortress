package org.minefortress.tasks;

import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record TaskBlockInfo(
        HitResult hitResult,
        Direction horizontalDirection,
        Item placingItem,
        BlockPos pos
) {}
