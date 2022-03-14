package org.minefortress.selections;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.network.ServerboundSimpleSelectionTaskPacket;
import org.minefortress.tasks.TaskType;
import org.minefortress.utils.BlockUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class WallsSelection extends Selection {

    protected final List<BlockPos> corners = new ArrayList<>();
    private ClickType clickType;
    protected int upDelta;

    protected List<BlockPos> selection;
    private List<Pair<Vec3i, Vec3i>> selectionSizes;

    @Override
    public boolean isSelecting() {
        return !corners.isEmpty();
    }

    @Override
    public boolean needUpdate(BlockPos pickedBlock, int upDelta) {
        if(corners.isEmpty()) return false;
        if(this.upDelta != upDelta) return true;
        BlockPos last = getLastCorner();

        if(corners.size() == 1) return true;

        return !last.equals(pickedBlock);
    }


    @Override
    public void update(BlockPos pickedBlock, final int upDelta) {
        pickedBlock = pickedBlock.toImmutable();
        this.upDelta = upDelta;
        updateCorners(pickedBlock);
        ArrayList<Pair<BlockPos, BlockPos>> cornerPairs = getCornerPairs();
        this.selection = getSelection(upDelta, cornerPairs);
        this.selectionSizes = getSelectionSizes(upDelta, cornerPairs);
    }

    protected List<Pair<Vec3i, Vec3i>> getSelectionSizes(int upDelta, ArrayList<Pair<BlockPos, BlockPos>> cornerPairs) {
        return cornerPairs
                .stream()
                .map(p -> Selection.getSelectionSize(p.getFirst(), p.getSecond().up(upDelta)))
                .toList();
    }

    protected List<BlockPos> getSelection(int upDelta, ArrayList<Pair<BlockPos, BlockPos>> cornerPairs) {
        return cornerPairs
                .stream()
                .map(p -> {
                    final BlockPos start = p.getFirst();
                    final BlockPos end = p.getSecond();
                    return BlockPos.iterate(start, new BlockPos(end.getX(), start.getY()+upDelta, end.getZ()));
                })
                .flatMap(WallsSelection::iterableToList)
                .toList();
    }

    private ArrayList<Pair<BlockPos, BlockPos>> getCornerPairs() {
        ArrayList<Pair<BlockPos, BlockPos>> cornerPairs = new ArrayList<>();
        if(corners.size() == 1) {
            cornerPairs.add(Pair.of(corners.get(0), corners.get(0)));
        } else {
            for (int i = 0; i < corners.size() - 1; i++) {
                BlockPos thisCorner = corners.get(i);
                BlockPos nextCorner = corners.get(i + 1);

                Pair<BlockPos, BlockPos> pair = Pair.of(thisCorner, nextCorner);
                cornerPairs.add(pair);
            }
        }
        return cornerPairs;
    }

    public static Stream<BlockPos> iterableToList(Iterable<BlockPos> iterable) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        for(BlockPos pos : iterable) {
            positions.add(pos.toImmutable());
        }

        return positions.stream();
    }

    private void updateCorners(BlockPos pickedBlock) {
        BlockPos last = getLastCorner();
        if(corners.size() == 1) {
            if(!corners.get(0).equals(pickedBlock)) {
                BlockPos subtract = pickedBlock.subtract(last);
                if(subtract.getX() == 0 || subtract.getZ() == 0)
                    corners.add(pickedBlock);
                else if(Math.abs(subtract.getX()) < Math.abs(subtract.getZ())) {
                    BlockPos newPicked = new BlockPos(last.getX(), pickedBlock.getY(), pickedBlock.getZ());
                    corners.add(newPicked);
                } else {
                    BlockPos newPicked = new BlockPos(pickedBlock.getX(), pickedBlock.getY(), last.getZ());
                    corners.add(newPicked);
                }
            }
            return;
        }

        BlockPos preLastCorner = getPreLastCorner();
        if(preLastCorner.equals(pickedBlock) || flatCloserThan(preLastCorner, pickedBlock, 2.5)) {
            corners.remove(corners.size() -1);
            return;
        }

        if (blockOnOneLineWithLastTwo(pickedBlock)) {
            setLast(pickedBlock);
        } else {
            LastTwoBlocksLine lastTwoBlocksLine = getLastTwoBlocksLine();

            BlockPos newLast;
            if(lastTwoBlocksLine == LastTwoBlocksLine.X)
                newLast = new BlockPos(last.getX(), last.getY(), pickedBlock.getZ());
            else if(lastTwoBlocksLine == LastTwoBlocksLine.Z)
                newLast = new BlockPos(pickedBlock.getX(), last.getY(), last.getZ());
            else
                throw new IllegalStateException("Two blocks are not on one line");

            setLast(newLast);

            if(flatCloserThan(newLast, pickedBlock, 2.5)) return;
            corners.add(pickedBlock);
        }
    }

    private static boolean flatCloserThan(Vec3i it, Vec3i other, double distance) {
        int d1 = it.getX() - other.getX();
        int d2 = it.getZ() - other.getZ();

        return (d1 * d1 + d2 * d2) < distance * distance;
    }



    private void setLast(BlockPos pickedBlock) {
        corners.set(corners.size() - 1, pickedBlock);
    }

    @Override
    public boolean selectBlock(
            ClientWorld level,
            Item mainHandItem,
            BlockPos pickedBlock,
            final int upDelta,
            ClickType click,
            final ClientPlayNetworkHandler connection,
            final HitResult hitResult
    ) {
        if(corners.isEmpty()) {
            corners.add(pickedBlock.toImmutable());
            this.clickType = click;
            return false;
        } else {
            if(this.clickType == click) {
                UUID supertaskUuid = UUID.randomUUID();
                final BlockState blockStateFromItem = BlockUtils.getBlockStateFromItem(mainHandItem);
                final TaskType taskType = mapClickTypeToTaskType(clickType);
                getCornerPairs()
                        .forEach(p -> {
                            UUID uuid = UUID.randomUUID();
                            ((FortressClientWorld)level).getClientTasksHolder().addTask(uuid, getSelection(), blockStateFromItem, taskType, supertaskUuid);
                            ServerboundSimpleSelectionTaskPacket packet = new ServerboundSimpleSelectionTaskPacket(
                                    uuid,
                                    taskType,
                                    p.getFirst(),
                                    p.getSecond().up(upDelta),
                                    hitResult,
                                    getSelectionType());

                            FortressClientNetworkHelper.send(FortressChannelNames.NEW_SELECTION_TASK, packet);
                        });
            }
            return true;
        }
    }

    protected SelectionType getSelectionType() {
        return SelectionType.WALLS;
    }

    private boolean blockOnOneLineWithLastTwo(BlockPos pos) {
        BlockPos lastCorner = getLastCorner();
        LastTwoBlocksLine lastTwoBlocksLine = getLastTwoBlocksLine();

        if(lastTwoBlocksLine == LastTwoBlocksLine.Z) {
            return lastCorner.getZ() == pos.getZ();
        }

        if(lastTwoBlocksLine == LastTwoBlocksLine.X) {
            return lastCorner.getX() == pos.getX();
        }

        throw new IllegalStateException("Last two blocks are not on one line");
    }

    private LastTwoBlocksLine getLastTwoBlocksLine() {
        BlockPos lastCorner = getLastCorner();
        BlockPos preLastCorner = getPreLastCorner();

        boolean lastTwoBlocksOnOneLineByX = lastCorner.getX() == preLastCorner.getX();
        boolean lastTwoBlocksOnOneLineByZ = lastCorner.getZ() == preLastCorner.getZ();

        if(lastTwoBlocksOnOneLineByZ) {
            return LastTwoBlocksLine.Z;
        }


        if(lastTwoBlocksOnOneLineByX) {
            return LastTwoBlocksLine.X;
        }

        throw new IllegalStateException("Last two blocks are not on one line");
    }

    private BlockPos getPreLastCorner() {
        return corners.get(corners.size() - 2);
    }

    private BlockPos getLastCorner() {
        return corners.get(corners.size() - 1);
    }

    @Override
    public void setRendererDirty(final WorldRenderer renderer) {
        getCornerPairs()
                .forEach(p -> Selection.makeBlocksDirty(p.getFirst(), p.getSecond(), renderer));
    }

    @Override
    public Iterable<BlockPos> getSelection() {
        return this.selection!=null?this.selection:Collections.emptyList();
    }

    @Override
    public void reset() {
        this.corners.clear();
        this.clickType = null;
        this.upDelta = 0;
        this.selection = null;
        this.selectionSizes = null;
    }

    @Override
    public List<Pair<Vec3i, Vec3i>> getSelectionSize() {
        return this.selectionSizes!=null?this.selectionSizes:Collections.emptyList();
    }

    @Override
    public List<Pair<Vec3d, String>> getSelectionLabelsPosition() {
        if(corners.isEmpty()) return super.getSelectionLabelsPosition();

        final ArrayList<Pair<Vec3d, String>> labels = new ArrayList<>();
        for(int i = 0; i < corners.size() - 1; i++) {
            BlockPos first = corners.get(i);
            BlockPos second = corners.get(i + 1);

            final List<Pair<Vec3d, String>> selectionLabels = getSelectionLabels(first, second.up(upDelta), i == 0);
            labels.addAll(selectionLabels);
        }

        return labels;
    }

    enum LastTwoBlocksLine {
        X,
        Z
    }
}
