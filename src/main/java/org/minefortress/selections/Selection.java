package org.minefortress.selections;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.minefortress.ClickType;
import org.minefortress.tasks.TaskType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Selection {
    public abstract boolean isSelecting();

    public abstract boolean needUpdate(BlockPos pickedBlock, int upDelta);

    public abstract boolean selectBlock(
            ClientWorld level,
            Item mainHandItem,
            BlockPos pickedBlock,
            int upDelta,
            ClickType click,
            ClientPlayNetworkHandler clientPacketListener,
            HitResult hitResult
    );

    public abstract void update(
            BlockPos pickedBlock,
            int upDelta
    );

    public abstract void setRendererDirty(WorldRenderer renderer);

    public abstract Iterable<BlockPos> getSelection();

    public abstract void reset();

    public abstract List<Pair<Vec3i, Vec3i>> getSelectionSize();
    
    protected static Pair<Vec3i, Vec3i> getSelectionSize(BlockPos start, BlockPos end) {
        if(end == null || start == null) return null;
        Vec3i diff = start.subtract(end);
        Vec3i absDiff = new Vec3i(Math.abs(diff.getX()), Math.abs(diff.getY()), Math.abs(diff.getZ()))
                .add(1, 1, 1)
                .multiply(16);


        Vec3i selStart = new Vec3i(
                getMin(start, end, Direction.Axis.X),
                getMin(start, end, Direction.Axis.Y),
                getMin(start, end, Direction.Axis.Z)
        );

        return Pair.of(absDiff, selStart);
    }

    protected static void makeBlocksDirty(BlockPos start, BlockPos end, WorldRenderer renderer) {
        if(start == null || end == null) return;

        renderer.scheduleTerrainUpdate();

        int minX = getMin(start, end, Direction.Axis.X);
        int minY = getMin(start, end, Direction.Axis.Y);
        int minZ = getMin(start, end, Direction.Axis.Z);

        int maxX = getMax(start, end, Direction.Axis.X);
        int maxY = getMax(start, end, Direction.Axis.Y);
        int maxZ = getMax(start, end, Direction.Axis.Z);

        renderer.scheduleBlockRenders(minX, minY, minZ, maxX, maxY, maxZ);
    }

    protected static TaskType mapClickTypeToTaskType(ClickType clickType) {
        return switch (clickType) {
            case BUILD -> TaskType.BUILD;
            case REMOVE -> TaskType.REMOVE;
        };
    }

    private static int getMin(BlockPos block1, BlockPos block2, Direction.Axis axis) {
        return Math.min(block1.getComponentAlongAxis(axis), block2.getComponentAlongAxis(axis));
    }

    private static int getMax(BlockPos block1, BlockPos block2, Direction.Axis axis) {
        return Math.max(block1.getComponentAlongAxis(axis), block2.getComponentAlongAxis(axis));
    }

    public List<Pair<Vec3d, String>> getSelectionLabelsPosition() {
        return Collections.emptyList();
    }

    protected static List<Pair<Vec3d, String>> getSelectionLabels(BlockPos selectionEnd, BlockPos selectionStart) {
        return getSelectionLabels(selectionEnd, selectionStart, true);
    }

    protected static List<Pair<Vec3d, String>> getSelectionLabels(BlockPos selectionEnd, BlockPos selectionStart, boolean showYDelta) {
        if(selectionEnd != null) {
            Vec3i difference = selectionEnd.subtract(selectionStart);
            Vec3d direction = new Vec3d(MathHelper.sign(difference.getX()), MathHelper.sign(difference.getY()), MathHelper.sign(difference.getZ()));
            difference = difference.add(direction.x, direction.y, direction.z);

            Vec3d differenceVec = new Vec3d(difference.getX(), difference.getY(), difference.getZ());

            final double dy = Math.max(0, differenceVec.y);

            Vec3d xVec = new Vec3d(selectionStart.getX() + differenceVec.x / 2f, selectionStart.getY() + dy, selectionStart.getZ());
            Vec3d yVec = new Vec3d(selectionStart.getX() + differenceVec.x, selectionStart.getY() + dy, selectionStart.getZ() + differenceVec.z / 2f);
            Vec3d zVec = new Vec3d(selectionStart.getX(), selectionStart.getY() + dy, selectionStart.getZ() + differenceVec.z / 2f);

            final ArrayList<Pair<Vec3d, String>> labels = new ArrayList<>();
            if(Math.abs(difference.getX()) > 1) {
                labels.add(new Pair<>(xVec, Math.abs(difference.getX()) + ""));
            }

            if(showYDelta && Math.abs(difference.getY()) > 1) {
                labels.add(new Pair<>(yVec, Math.abs(difference.getY()) + ""));
            }

            if(Math.abs(difference.getZ()) > 1) {
                labels.add(new Pair<>(zVec, Math.abs(difference.getZ()) + ""));
            }

            return labels;
        }
        return Collections.emptyList();
    }
}
