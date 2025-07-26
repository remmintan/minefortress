package net.remmintan.mods.minefortress.core.dtos.tasks;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;

import java.util.List;

public record TaskInformationDto(BlockPos pos, List<BlockPos> positions, TaskType type) {
}
