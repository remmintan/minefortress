package net.remmintan.mods.minefortress.core.interfaces.selections;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ISelectionManager extends ISelectionModelBuilderInfoProvider, ISelectionInfoProvider {

    void selectBlock(BlockPos blockPos);

    void selectBlock(BlockPos blockPos, BlockState blockState);

    void moveSelectionUp();

    void moveSelectionDown();

    void tickSelectionUpdate(@Nullable BlockPos blockPos, Direction clickedFace);

    void toggleSelectionType();

    void setSelectionType(ISelectionType type);

    ISelectionType getCurrentSelectionType();

    void resetSelection();

    List<Pair<Vec3d, String>> getLabels();

    int getSelectionTypeIndex();

}
