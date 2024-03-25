package net.remmintan.gobi;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionType;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundSimpleSelectionTaskPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TwoDotsSelection extends Selection {

    protected BlockPos selectionStart;
    private BlockPos selectionEnd;
    private List<BlockPos> selection;
    private ClickType clickType;

    @Override
    public boolean isSelecting() {
        return selectionStart != null;
    }

    @Override
    public boolean needUpdate(BlockPos pickedBlock, int upSelectionDelta) {
        if(pickedBlock == null) return false;
        pickedBlock = pickedBlock.up(upSelectionDelta);
        return this.selectionStart!=null && !pickedBlock.equals(this.selectionEnd) && super.needUpdate(pickedBlock, upSelectionDelta);
    }

    @Override
    protected Optional<BlockPos> getSelectionStart() {
        return Optional.ofNullable(selectionStart);
    }

    @Override
    public boolean selectBlock(World level,
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
            if(pickedBlock != null && hitResult instanceof BlockHitResult && click == this.clickType && connection != null && selectionEnd != null) {
                final var taskType = mapClickTypeToTaskType(clickType);
                final var selectionManager = CoreModUtils.getMineFortressManagersProvider().get_PawnsSelectionManager();
                final var selectedPawnsIds = selectionManager.getSelectedPawnsIds();
                final var packet = new ServerboundSimpleSelectionTaskPacket(
                        UUID.randomUUID(),
                        taskType,
                        this.selectionStart,
                        this.selectionEnd,
                        hitResult,
                        getSelectionType().name(),
                        getSelection(),
                        selectedPawnsIds
                );
                FortressClientNetworkHelper.send(FortressChannelNames.NEW_SELECTION_TASK, packet);
                selectionManager.resetSelection();
            }
            return true;
        }
    }

    protected ISelectionType getSelectionType() {
        return SelectionType.SQUARES;
    }

    @Override
    public void update(BlockPos pickedBlock, int upDelta) {
        this.selectionEnd = pickedBlock.up(upDelta);
        this.selection = getIterableForSelectionUpdate(this.selectionStart, this.selectionEnd);
    }

    protected List<BlockPos> getIterableForSelectionUpdate(BlockPos selectionStart, BlockPos selectionEnd) {
        return StreamSupport.stream(BlockPos.iterate(selectionStart, selectionEnd).spliterator(), false)
                .map(BlockPos::toImmutable)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlockPos> getSelection() {
        return this.selection != null?this.selection:Collections.emptyList();
    }

    @Override
    public void reset() {
        this.selectionStart = null;
        this.selectionEnd = null;
        this.clickType = null;
        this.selection = null;
    }

    public List<Pair<Vec3i, Vec3i>> getSelectionDimensions() {
        Pair<Vec3i, Vec3i> size = getSelectionDimensions(selectionStart, selectionEnd);
        return Collections.singletonList(size);
    }

    @Override
    public List<Pair<Vec3d, String>> getSelectionLabelsPosition() {
        final BlockPos selectionEnd = this.selectionEnd;
        final BlockPos selectionStart = this.selectionStart;
        return getSelectionLabels(selectionEnd, selectionStart);
    }


}
