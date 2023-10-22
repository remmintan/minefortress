package net.remmintan.gobi;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.remmintan.gobi.helpers.TreeBlocks;
import net.remmintan.gobi.helpers.TreeHelper;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksInformationHolder;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundCutTreesTaskPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;

import java.util.*;


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
                if(level instanceof ITasksInformationHolder holder) {
                    holder.get_ClientTasksHolder().addTask(newTaskId, getSelection(), TaskType.REMOVE);
                }
                final var packet = new ServerboundCutTreesTaskPacket(newTaskId, Collections.unmodifiableList(treeRoots));
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
            if(TreeHelper.isLog(blockState)) {
                final Optional<BlockPos> rootDownFromLog = findRootDownFromLog(pos, world);
                if(rootDownFromLog.isPresent()) {
                    treeRoots.add(rootDownFromLog.get());
                    continue;
                }
            }

            if(blockState.isAir() || TreeHelper.isLeaves(blockState)) {
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
            final var treeBlocks = TreeHelper.getTreeBlocks(root, world);
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
        } while(TreeHelper.isLog(cursorState));

        if(cursorState.isAir()) return Optional.empty();
        return Optional.of(cursor.up());
    }

    private Optional<BlockPos> findRootDownFromAirOrLeaves(BlockPos start, World world) {
        BlockPos cursor = start;
        BlockState cursorState;

        do {
            cursor = cursor.down();
            cursorState = world.getBlockState(cursor);
        } while(cursorState.isAir() || TreeHelper.isLeaves(cursorState));

        if(TreeHelper.isLog(cursorState)) return findRootDownFromLog(cursor, world);
        return Optional.empty();
    }

    private Optional<BlockPos> findRootUpFromGround(BlockPos start, World world) {
        BlockPos cursor = start;
        BlockState cursorState;

        do {
            cursor = cursor.up();
            cursorState = world.getBlockState(cursor);
        } while(!TreeHelper.isLog(cursorState) && !cursorState.isAir());

        if(TreeHelper.isLog(cursorState)) return Optional.of(cursor);
        return Optional.empty();
    }


}
