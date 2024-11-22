package net.remmintan.gobi;


import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.building.BuildingHelper;
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelection;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionType;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SelectionManager implements ISelectionManager {

    private final MinecraftClient client;

    private int selectionTypeIndex = 0;
    private ISelectionType currentSelectionType = SelectionType.SQUARES;
    private ISelection selection = new TwoDotsSelection();

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

    @Override
    public void selectBlock(BlockPos blockPos) {
        selectBlock(blockPos, ClickType.REMOVE, null);
    }

    @Override
    public void selectBlock(BlockPos blockPos, BlockState blockState) {
        selectBlock(blockPos, ClickType.BUILD, blockState);
    }

    @Override
    public void moveSelectionUp() {
        upSelectionDelta++;
    }

    @Override
    public void moveSelectionDown() {
        upSelectionDelta--;
    }

    @Override
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
            final var provider = CoreModUtils.getMineFortressManagersProvider();
            final var clientManager = provider.get_ClientFortressManager();
            if((clickType == ClickType.BUILD || clickType == ClickType.ROADS)&& clickingBlockState != null) {
                if(clientManager.isSurvival()){
                    if(isCountableBlock(clickingBlockState)) {
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
        if(!inCorrectState) {
            return new Vector4f((170f/255f), 0.0f, 0.0f, 0.5f);
        }
        float green = (this.clickType == ClickType.BUILD || this.clickType == ClickType.ROADS)? (170f/255f) : 0.0f;
        return new Vector4f(0.0f, green, 0.0f, 0.5f);
    }

    public List<Pair<Vec3i, Vec3i>> getSelectionDimensions() {
        return this.selection.getSelectionDimensions();
    }

    private static boolean isCountableBlock(BlockState state) {
        if(!state.getFluidState().isEmpty()) return false;
        final var block = state.getBlock();
        if(block == Blocks.FIRE) return false;
        if(block == Blocks.AIR) return false;
        return block != Blocks.BARRIER;
    }

    @Override
    public void toggleSelectionType() {
        this.upSelectionDelta = 0;
        resetSelection();

        ISelectionType[] types = Arrays
                .stream(SelectionType.values())
                .filter(type -> type != SelectionType.TREE)
                .filter(type -> type != SelectionType.ROADS)
                .toArray(ISelectionType[]::new);
        if(++selectionTypeIndex >= types.length) {
            selectionTypeIndex = 0;
        }
        currentSelectionType = types[selectionTypeIndex];
        selection = currentSelectionType.generate();
    }

    @Override
    public void setSelectionType(ISelectionType type) {
        this.upSelectionDelta = 0;
        resetSelection();

        currentSelectionType = type;
        selectionTypeIndex = type.ordinal();
        selection = currentSelectionType.generate();
    }

    @Override
    public ISelectionType getCurrentSelectionType() {
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

    @Override
    public void resetSelection() {
        if(!this.isSelecting()) return;
        selection.reset();
        this.setSelectionType(SelectionType.SQUARES);
        this.clickType = null;
        this.upSelectionDelta = 0;
        this.inCorrectState = true;
    }

    @Override
    public ClickType getClickType() {
        return clickType;
    }

    @Override
    public List<Pair<Vec3d, String>> getLabels() {
        return selection != null ? selection.getSelectionLabelsPosition() : Collections.emptyList();
    }

    @Override
    public int getSelectionTypeIndex() {
        return selectionTypeIndex;
    }

    public boolean isNeedsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    @Override
    public boolean isInCorrectState() {
        return inCorrectState;
    }
}
