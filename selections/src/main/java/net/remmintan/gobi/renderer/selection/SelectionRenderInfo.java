package net.remmintan.gobi.renderer.selection;


import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType;
import org.joml.Vector4f;

import java.util.List;

public record SelectionRenderInfo(ClickType clickType, Vector4f color, List<BlockPos> positions, BlockState blockState, List<Pair<Vec3i, Vec3i>> selectionDimensions) {}
