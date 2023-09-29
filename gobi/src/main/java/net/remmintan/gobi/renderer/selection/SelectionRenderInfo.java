package org.minefortress.selections.renderer.selection;


import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector4f;
import org.minefortress.selections.ClickType;

import java.util.List;

public record SelectionRenderInfo(ClickType clickType, Vector4f color, List<BlockPos> positions, BlockState blockState, List<Pair<Vec3i, Vec3i>> selectionDimensions) {}
