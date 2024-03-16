package net.remmintan.mods.minefortress.core.dtos.tasks;

import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;

import java.util.List;
import java.util.UUID;

public record TaskInformationDto(UUID id, List<BlockPos> positions, TaskType type) {}
