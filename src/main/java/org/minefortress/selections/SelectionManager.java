package org.minefortress.selections;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;
import org.minefortress.interfaces.FortressWorldRenderer;
import org.minefortress.mixins.interfaces.FortressDimensionTypeMixin;

import java.util.*;

public class SelectionManager implements FortressWorldRenderer {

    private final MinecraftClient client;

    private int selectionTypeIndex = 0;
    private SelectionType currentSelectionType = SelectionType.SQUARES;
    private Selection selection = new TwoDotsSelection();

    private ClickType clickType;
    private BlockState clickingBlockState = null;
    private int upSelectionDelta = 0;

    private final Set<BlockPos> selectedBlocks = new HashSet<>();

    @Override
    public @Nullable BlockState getClickingBlock() {
        return clickingBlockState;
    }

    @Override
    public Set<BlockPos> getSelectedBlocks() {
        return selectedBlocks;
    }

    private void updateSelectedBlocks() {
        HitResult hitResult = client.crosshairTarget;
        ClientWorld level = client.world;
        if(hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && level != null) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            if(!level.getBlockState(blockPos).isAir()) {
                this.selectedBlocks.clear();
                if(this.clickType == ClickType.BUILD && clickingBlockState != null) {
                    Iterator<BlockPos> selectionIterator = this.getCurrentSelection();
                    while (selectionIterator.hasNext()) {
                        this.selectedBlocks.add(selectionIterator.next().toImmutable());
                    }
                }
            }
        }
    }

    public SelectionManager(MinecraftClient client) {
        this.client = client;
    }

    public void selectBlock(BlockPos blockPos) {
        selectBlock(blockPos, ClickType.REMOVE, null);
    }

    public void selectBlock(BlockPos blockPos, BlockState blockState) {
        selectBlock(blockPos, ClickType.BUILD, blockState);
    }

    public void moveSelectionUp() {
        if(this.clickType == ClickType.REMOVE && upSelectionDelta == 0) return;
        upSelectionDelta++;
    }

    public void moveSelectionDown() {
        if (clickType == ClickType.BUILD && upSelectionDelta == 0) return;
        upSelectionDelta--;
    }

    public BlockState getClickingBlockState() {
        return clickingBlockState;
    }

    public void tickSelectionUpdate(BlockPos blockPos, Direction clickedFace) {
        final ClientWorld level = client.world;
        if(level == null || level.getDimension().equals(FortressDimensionTypeMixin.getNether()) || level.getDimension().equals(FortressDimensionTypeMixin.getEnd())) {
            if(selection.isSelecting()) {
                selection.reset();
            }
            return;
        }

        BlockPos pickedPos = this.clickType == ClickType.BUILD? blockPos.offset(clickedFace) : blockPos;
        if(this.selection.needUpdate(pickedPos, upSelectionDelta)) {
            this.selection.update(pickedPos, upSelectionDelta);
            this.selection.setRendererDirty(client.worldRenderer);
        }

        updateSelectedBlocks();
    }

    public Vector4f getClickColors() {
        float green = this.clickType == ClickType.BUILD? (170f/255f) : 0.0f;
        return new Vector4f(0.0f, green, 0.0f, 0.5f);
    }

    public Iterator<BlockPos> getCurrentSelection() {
        return this.selection.getSelection().iterator();
    }

    public List<Pair<Vec3i, Vec3i>> getSelectionSize() {
        return this.selection.getSelectionSize();
    }

    public void toggleSelectionType() {
        this.upSelectionDelta = 0;
        resetSelection();

        SelectionType[] types = SelectionType.values();
        if(++selectionTypeIndex >= types.length) {
            selectionTypeIndex = 0;
        }
        currentSelectionType = types[selectionTypeIndex];
        selection = currentSelectionType.generate();
    }

    public void setSelectionType(SelectionType type) {
        this.upSelectionDelta = 0;
        resetSelection();

        currentSelectionType = type;
        selectionTypeIndex = type.ordinal();
    }

    public SelectionType getCurrentSelectionType() {
        return currentSelectionType;
    }

    private void selectBlock(BlockPos blockPos, ClickType click, BlockState blockState) {
        final ClientWorld level = client.world;
        if(level == null || level.getDimension().equals(FortressDimensionTypeMixin.getNether()) || level.getDimension().equals(FortressDimensionTypeMixin.getEnd())) return;

        if(blockState == null && click == ClickType.BUILD) {
            resetSelection();
            return;
        } else {
            this.clickType = click;
            this.clickingBlockState = blockState;
        }

        Item mainHandItem = Optional.ofNullable(client.player).map(it -> it.getStackInHand(Hand.MAIN_HAND).getItem()).orElse(null);

        boolean result = this.selection.selectBlock(
                client.world,
                mainHandItem,
                blockPos,
                upSelectionDelta,
                click,
                this.client.getNetworkHandler(),
                this.client.crosshairTarget
        );
        if(result) resetSelection();
    }

    public boolean isSelecting() {
        return selection.isSelecting();
    }

    public void resetSelection() {
        selection.setRendererDirty(client.worldRenderer);
        selection.reset();
        this.clickType = null;
        this.upSelectionDelta = 0;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public List<Pair<Vec3d, String>> getLabels() {
        return selection != null ? selection.getSelectionLabelsPosition() : Collections.emptyList();
    }



}
