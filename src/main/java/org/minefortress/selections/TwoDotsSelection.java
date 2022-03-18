package org.minefortress.selections;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TwoDotsSelection extends Selection {

    protected BlockPos selectionStart;
    private BlockPos selectionEnd;
    private Iterable<BlockPos> selection;
    private ClickType clickType;

    @Override
    public boolean isSelecting() {
        return selectionStart != null;
    }

    @Override
    public boolean needUpdate(BlockPos pickedBlock, int upSelectionDelta) {
        pickedBlock = pickedBlock.up(upSelectionDelta);
        return this.selectionStart!=null && !pickedBlock.equals(this.selectionEnd);
    }

    @Override
    public boolean selectBlock(ClientWorld level,
                               Item item,
                               BlockPos pickedBlock,
                               int upSelectionDelta,
                               ClickType click,
                               ClientPlayNetworkHandler connection,
                               HitResult hitResult) {
        if (this.selectionStart == null) {
            this.selectionStart = pickedBlock;
            this.clickType = click;
            return false;
        } else {
            if(pickedBlock == null && hitResult instanceof BlockHitResult && click == this.clickType && connection != null) {
                UUID newTaskId = UUID.randomUUID();
                TaskType taskType = mapClickTypeToTaskType(clickType);
                final BlockState blockStateFromItem = BlockUtils.getBlockStateFromItem(item);
                ((FortressClientWorld)level).getClientTasksHolder().addTask(newTaskId, getSelection(), blockStateFromItem, taskType);
                ServerboundSimpleSelectionTaskPacket packet = new ServerboundSimpleSelectionTaskPacket(
                        newTaskId,
                        taskType,
                        this.selectionStart,
                        this.selectionEnd,
                        hitResult,
                        getSelectionType());


                FortressClientNetworkHelper.send(FortressChannelNames.NEW_SELECTION_TASK, packet);
            }
            return true;
        }
    }

    protected SelectionType getSelectionType() {
        return SelectionType.SQUARES;
    }

    @Override
    public void update(BlockPos pickedBlock, int upDelta) {
        this.selectionEnd = pickedBlock.up(upDelta);
        this.selection = getIterableForSelectionUpdate(this.selectionStart, this.selectionEnd);
    }

    protected Iterable<BlockPos> getIterableForSelectionUpdate(BlockPos selectionStart, BlockPos selectionEnd) {
        return BlockPos.iterate(selectionStart, selectionEnd);
    }

    @Override
    public void setRendererDirty(WorldRenderer renderer) {
        makeBlocksDirty(selectionStart, selectionEnd, renderer);
    }

    @Override
    public Iterable<BlockPos> getSelection() {
        return this.selection != null?this.selection:Collections.emptyList();
    }

    @Override
    public void reset() {
        this.selectionStart = null;
        this.selectionEnd = null;
        this.clickType = null;
        this.selection = null;
    }

    public List<Pair<Vec3i, Vec3i>> getSelectionSize() {
        Pair<Vec3i, Vec3i> size = getSelectionSize(selectionStart, selectionEnd);
        return Collections.singletonList(size);
    }

    @Override
    public List<Pair<Vec3d, String>> getSelectionLabelsPosition() {
        final BlockPos selectionEnd = this.selectionEnd;
        final BlockPos selectionStart = this.selectionStart;
        return getSelectionLabels(selectionEnd, selectionStart);
    }


}
