package org.minefortress.selections;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.network.c2s.ServerboundCutTreesTaskPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.tasks.TaskType;
import org.minefortress.utils.TreeBlocks;

import java.util.*;

import static org.minefortress.utils.TreeHelper.*;

public class TreeSelection extends Selection {

    private BlockPos start;
    private BlockPos end;

    private final List<BlockPos> treeRoots = new ArrayList<>();
    private final List<BlockPos> selectedTreeBlocks = new ArrayList<>();

    @Override
    public boolean isSelecting() {
        return start != null;
    }

    @Override
    public boolean needUpdate(BlockPos pickedBlock, int upDelta) {
        return start != null && !Objects.equals(pickedBlock, end);
    }

    @Override
    public boolean selectBlock(World level, Item mainHandItem, BlockPos pickedBlock, int upDelta, ClickType click, ClientPlayNetworkHandler clientPacketListener, HitResult hitResult) {
        if(click == ClickType.BUILD) {
            return start != null;
        }

        if(start == null) {
            start = pickedBlock;
            return false;
        } else {
            if(!treeRoots.isEmpty()) {
                this.selectedTreeBlocks.remove(start);
                final UUID newTaskId = UUID.randomUUID();
                ((FortressClientWorld)level).getClientTasksHolder().addTask(newTaskId, getSelection(), TaskType.REMOVE);
                final ServerboundCutTreesTaskPacket packet = new ServerboundCutTreesTaskPacket(newTaskId, Collections.unmodifiableList(treeRoots));
                FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_CUT_TREES_TASK, packet);
            }

            return true;
        }
    }

    @Override
    public void update(BlockPos pickedBlock, int upDelta) {
        if(start != null) {
            end = pickedBlock;
            updateSelection();
        }
    }

    @Override
    public List<BlockPos> getSelection() {
        return selectedTreeBlocks;
    }

    @Override
    public void reset() {
        start = null;
        treeRoots.clear();
        selectedTreeBlocks.clear();
    }

    @Override
    public List<Pair<Vec3i, Vec3i>> getSelectionDimensions() {
        return Collections.emptyList();
    }

    private void updateSelection() {
        final World world = MinecraftClient.getInstance().world;
        if (world == null) return;

        updateTreeRoots(world);
        updateTreeData(world);
        if(start != null) {
            selectedTreeBlocks.add(start);
        }


    }

    private void updateTreeRoots(World world) {
        treeRoots.clear();
        if(start == null || end == null) return;
        int minY = Math.min(start.getY(), end.getY());

        BlockPos flatStart = new BlockPos(start.getX(), minY, start.getZ());
        BlockPos flatEnd = new BlockPos(end.getX(), minY, end.getZ());

        for(BlockPos pos: BlockPos.iterate(flatStart, flatEnd)) {
            pos = pos.toImmutable();
            final BlockState blockState = world.getBlockState(pos);
            if(blockState.isAir()) continue;
            if(isLog(blockState)) {
                final Optional<BlockPos> rootDownFromLog = findRootDownFromLog(pos, world);
                if(rootDownFromLog.isPresent()) {
                    treeRoots.add(rootDownFromLog.get());
                    continue;
                }
            }

            if(blockState.isAir() || isLeaves(blockState)) {
                final Optional<BlockPos> rootDownFromLeaves = findRootDownFromAirOrLeaves(pos, world);
                if(rootDownFromLeaves.isPresent()) {
                    treeRoots.add(rootDownFromLeaves.get());
                    continue;
                }
            }

            final Optional<BlockPos> rootUpFromGround = findRootUpFromGround(pos, world);
            rootUpFromGround.ifPresent(treeRoots::add);
        }
    }
    private void updateTreeData(World world) {
        this.selectedTreeBlocks.clear();
        for(BlockPos root: new ArrayList<>(treeRoots)) {
            final Optional<TreeBlocks> treeBlocks = getTreeBlocks(root, world);
            if(treeBlocks.isEmpty()){
                treeRoots.remove(root);
            } else {
                final TreeBlocks tree = treeBlocks.get();
                this.selectedTreeBlocks.addAll(tree.getTreeBlocks());
                this.selectedTreeBlocks.addAll(tree.getLeavesBlocks());
            }
        }
    }

    private Optional<BlockPos> findRootDownFromLog(BlockPos start, World world) {
        BlockPos cursor = start;
        BlockState cursorState;

        do {
            cursor = cursor.down();
            cursorState = world.getBlockState(cursor);
        } while(isLog(cursorState));

        if(cursorState.isAir()) return Optional.empty();
        return Optional.of(cursor.up());
    }

    private Optional<BlockPos> findRootDownFromAirOrLeaves(BlockPos start, World world) {
        BlockPos cursor = start;
        BlockState cursorState;

        do {
            cursor = cursor.down();
            cursorState = world.getBlockState(cursor);
        } while(cursorState.isAir() || isLeaves(cursorState));

        if(isLog(cursorState)) return findRootDownFromLog(cursor, world);
        return Optional.empty();
    }

    private Optional<BlockPos> findRootUpFromGround(BlockPos start, World world) {
        BlockPos cursor = start;
        BlockState cursorState;

        do {
            cursor = cursor.up();
            cursorState = world.getBlockState(cursor);
        } while(!isLog(cursorState) && !cursorState.isAir());

        if(isLog(cursorState)) return Optional.of(cursor);
        return Optional.empty();
    }


}
