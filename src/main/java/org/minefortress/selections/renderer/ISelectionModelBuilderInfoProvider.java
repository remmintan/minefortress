package org.minefortress.selections.renderer;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector4f;
import org.minefortress.selections.ClickType;

import java.util.List;

public interface ISelectionModelBuilderInfoProvider {

    default ClickType getClickType() {
        return ClickType.REMOVE;
    }
    Vector4f getClickColor();
    List<BlockPos> getSelectedBlocks();
    default BlockState getClickingBlock() {
        return Blocks.DIRT.getDefaultState();
    }
    List<Pair<Vec3i, Vec3i>> getSelectionDimensions();

}
