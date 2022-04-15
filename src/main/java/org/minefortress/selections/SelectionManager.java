package org.minefortress.selections;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.dimension.DimensionType;
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

    private boolean selectionHidden = false;

    public SelectionManager(MinecraftClient client) {
        this.client = client;
    }

    public void toggleSelectionVisibility() {
        this.selectionHidden = !this.selectionHidden;
    }

    public boolean isSelectionHidden() {
        return selectionHidden;
    }

    @Override
    public @Nullable BlockState getClickingBlock() {
        return clickingBlockState;
    }

    @Override
    public List<BlockPos> getSelectedBlocks() {
        return this.selection.getSelection();
    }

    public void selectBlock(BlockPos blockPos) {
        selectBlock(blockPos, ClickType.REMOVE, null);
    }

    public void selectBlock(BlockPos blockPos, BlockState blockState) {
        selectBlock(blockPos, ClickType.BUILD, blockState);
    }

    public void moveSelectionUp() {
        upSelectionDelta++;
    }

    public void moveSelectionDown() {
        upSelectionDelta--;
    }

    public void tickSelectionUpdate(BlockPos blockPos, Direction clickedFace) {
        if(isNotOverworld()) {
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
    }

    public Vector4f getClickColor() {
        float green = this.clickType == ClickType.BUILD? (170f/255f) : 0.0f;
        return new Vector4f(0.0f, green, 0.0f, 0.5f);
    }

    public List<Pair<Vec3i, Vec3i>> getSelectionSize() {
        return this.selection.getSelectionSize();
    }

    public void toggleSelectionType() {
        this.upSelectionDelta = 0;
        resetSelection();

        SelectionType[] types = Arrays
                .stream(SelectionType.values())
                .filter(type -> type != SelectionType.TREE)
                .filter(type -> type != SelectionType.ROADS)
                .toArray(SelectionType[]::new);
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
        selection = currentSelectionType.generate();
    }

    public SelectionType getCurrentSelectionType() {
        return currentSelectionType;
    }

    private void selectBlock(BlockPos blockPos, ClickType click, BlockState blockState) {
        if(isNotOverworld()) return;

        if((blockState == null || selection instanceof TreeSelection) && click == ClickType.BUILD) {
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

    private boolean isNotOverworld() {
        final ClientWorld level = client.world;
        if (level == null) {
            return true;
        } else {
            final DimensionType dimension = level.getDimension();
            return dimension.equals(FortressDimensionTypeMixin.getNether()) ||
                    dimension.equals(FortressDimensionTypeMixin.getEnd());
        }
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

    public int getSelectionTypeIndex() {
        return selectionTypeIndex;
    }
}
