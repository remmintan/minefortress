package org.minefortress.entity.ai;

import com.google.common.collect.AbstractIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.minefortress.selections.SelectionType;

import java.util.ArrayList;
import java.util.List;

public class PathUtils {

    public static Iterable<BlockPos> getLadderSelection(BlockPos selectionStart, BlockPos selectionEnd, Direction.Axis axis) {
        return getLadderSelection(selectionStart, selectionStart, selectionEnd, axis);
    }

    public static Iterable<BlockPos> getLadderSelection(BlockPos globalStart, BlockPos selectionStart, BlockPos selectionEnd, Direction.Axis axis) {
        List<BlockPos> blocks = new ArrayList<>();

        for(BlockPos pos: BlockPos.iterate(selectionStart, selectionEnd)) {
            int deltaAxis = findDeltaForAxis(globalStart, pos, axis);
            int deltaY = findDeltaForAxis(globalStart, pos, Direction.Axis.Y);
            if(deltaY <= deltaAxis) {
                blocks.add(pos.toImmutable());
            }
        }

        return blocks;
    }

    public static int findDeltaForAxis(BlockPos selectionStart, BlockPos selectionEnd, Direction.Axis axis) {
        int start = selectionStart.getComponentAlongAxis(axis);
        int end = selectionEnd.getComponentAlongAxis(axis);
        return Math.abs(end - start);
    }

    public static Iterable<BlockPos> fromStartToEnd(BlockPos start, BlockPos end, SelectionType selectionType) {
        Vec3i moveDirection = getDirection(start, end);
        Vec3i diff = diff(start, end).add(moveDirection);

        Vec3i dimensions = new Vec3i(Math.abs(diff.getX()), Math.abs(diff.getY()), Math.abs(diff.getZ()));
        int totalBlocks = dimensions.getX() * dimensions.getY() * dimensions.getZ();

        return () -> new AbstractIterator<>() {
            private final BlockPos.Mutable cursor = new BlockPos.Mutable();
            private int index;

            @Override
            protected BlockPos computeNext() {
                if(index >= totalBlocks) return super.endOfData();

                int dx = index % dimensions.getX();
                int dy = (index / dimensions.getX()) / dimensions.getZ();
                int dz = (index / dimensions.getX()) %dimensions.getZ();

                Vec3i offset = new Vec3i(
                        dx * moveDirection.getX(),
                        dy * moveDirection.getY(),
                        dz * moveDirection.getZ()
                );

                ++index;
                if(selectionType == SelectionType.WALLS_EVERY_SECOND) ++index;

                return cursor.set(start.add(offset));
            }
        };
    }

    public static Vec3i getDirection(BlockPos start, BlockPos end) {
        Vec3i diff = diff(start, end);
        return new Vec3i(sign(diff.getX()), sign(diff.getY()), sign(diff.getZ()));
    }

    private static Vec3i diff(BlockPos start, BlockPos end) {
        return end.subtract(start);
    }

    private static int sign(int i) {
        int nullableSign = MathHelper.sign(i);
        return nullableSign == 0? 1: nullableSign;
    }
}
