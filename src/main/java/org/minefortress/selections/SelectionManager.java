package org.minefortress.selections;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.interfaces.FortressWorldRenderer;
import org.minefortress.selections.renderer.ISelectionInfoProvider;
import org.minefortress.selections.renderer.ISelectionModelBuilderInfoProvider;
import org.minefortress.utils.BlockUtils;
import org.minefortress.utils.BuildingHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SelectionManager implements FortressWorldRenderer, ISelectionModelBuilderInfoProvider, ISelectionInfoProvider {

    private final MinecraftClient client;

    private int selectionTypeIndex = 0;
    private SelectionType currentSelectionType = SelectionType.SQUARES;
    private Selection selection = new TwoDotsSelection();

    private ClickType clickType;
    private BlockState clickingBlockState = null;
    private int upSelectionDelta = 0;

    private boolean needsUpdate = false;
    private boolean inCorrectState = true;

    public SelectionManager(MinecraftClient client) {
        this.client = client;
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

    public void tickSelectionUpdate(@Nullable BlockPos blockPos, Direction clickedFace) {
        if(blockPos == null) return;
        if(isNotOverworld()) {
            if(selection.isSelecting()) {
                this.resetSelection();
            }
            return;
        }

        BlockPos pickedPos;
        if (clickType == ClickType.BUILD) {
            if (BuildingHelper.canPlaceBlock(client.world, blockPos)) {
                pickedPos = blockPos;
            } else {
                pickedPos = blockPos.offset(clickedFace);
            }
        }else {
            pickedPos = blockPos;
        }
        if(this.selection.needUpdate(pickedPos, upSelectionDelta)) {
            this.selection.update(pickedPos, upSelectionDelta);
            this.setNeedsUpdate(true);

            if((clickType == ClickType.BUILD || clickType == ClickType.ROADS)&& clickingBlockState != null) {
                final var clientManager = ((FortressMinecraftClient) client).getFortressClientManager();
                if(clientManager.isSurvival()){
                    if(BlockUtils.isCountableBlock(clickingBlockState)) {
                        final var blocksAmount = this.selection.getSelection().size();
                        final var item = clickingBlockState.getBlock().asItem();
                        final var resourceManager = clientManager.getResourceManager();
                        final var itemStack = new ItemStack(item, blocksAmount);
                        inCorrectState = resourceManager.hasStacks(Collections.singletonList(itemStack));
                    } else {
                        inCorrectState = true;
                    }
                } else {
                    inCorrectState = true;
                }
            } else {
                inCorrectState = true;
            }

        }
    }

    public Vector4f getClickColor() {
        float green = (this.clickType == ClickType.BUILD || this.clickType == ClickType.ROADS)? (170f/255f) : 0.0f;
        return new Vector4f(0.0f, green, 0.0f, 0.5f);
    }

    public List<Pair<Vec3i, Vec3i>> getSelectionDimensions() {
        return this.selection.getSelectionDimensions();
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

        if(selection instanceof RoadsSelection && clickType == ClickType.BUILD)
            this.clickType = ClickType.ROADS;

        if(selection.isSelecting() && !this.inCorrectState) {
            this.resetSelection();
            return;
        }

        boolean result = this.selection.selectBlock(
                client.world,
                mainHandItem,
                blockPos,
                upSelectionDelta,
                this.clickType,
                this.client.getNetworkHandler(),
                this.client.crosshairTarget
        );
        if(result) resetSelection();
    }

    private boolean isNotOverworld() {
        final ClientWorld level = client.world;
        return level == null || level.getRegistryKey() != World.OVERWORLD;
    }

    public boolean isSelecting() {
        return selection.isSelecting();
    }

    public void resetSelection() {
        if(!this.isSelecting()) return;
        selection.reset();
        this.clickType = null;
        this.upSelectionDelta = 0;
        this.inCorrectState = true;
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

    public boolean isNeedsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    public boolean isInCorrectState() {
        return inCorrectState;
    }
}
