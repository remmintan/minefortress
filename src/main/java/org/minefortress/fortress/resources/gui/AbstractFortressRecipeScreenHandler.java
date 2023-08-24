package org.minefortress.fortress.resources.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.fortress.resources.client.FortressItemStack;
import org.minefortress.fortress.resources.server.ServerResourceManager;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.interfaces.FortressSimpleInventory;
import org.minefortress.network.c2s.ServerboundScrollCurrentScreenPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.interfaces.ScrollableHandler;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractFortressRecipeScreenHandler<T extends Inventory> extends AbstractRecipeScreenHandler<T> implements ScrollableHandler {

    private final SimpleInventory screenInventory = new SimpleInventory(999);
    private final VirtualInventory virtualInventory;
    private final ServerResourceManager serverResourceManager;

    private int clientCurrentRow = 5;

    protected final PlayerEntity player;
    protected final World world;

    public AbstractFortressRecipeScreenHandler(ScreenHandlerType<?> screenHandlerType, int i, ServerResourceManager resourceManager, PlayerEntity player) {
        super(screenHandlerType, i);
        this.serverResourceManager = resourceManager;
        this.player = player;
        this.world = player.getWorld();
        this.virtualInventory = Objects.nonNull(serverResourceManager) ? new VirtualInventory(serverResourceManager.getAllItems()) : null;
    }

    public int getRowsCount() {
        final var rowsCount = ((virtualInventory != null ? virtualInventory.size() : slots.size())) / 9;
        return rowsCount + (virtualInventory != null && virtualInventory.full() ? 1 : 0);
    }

    @Override
    public void setStackInSlot(int slot, int revision, ItemStack stack) {
        while (slots.size() <= slot) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new FortressNotInsertableSlot(this.screenInventory, column + clientCurrentRow * 9, 8 + column * 18, 80 + clientCurrentRow * 18));
            }
            clientCurrentRow++;
        }
        super.setStackInSlot(slot, revision, stack);
    }

    @Override
    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        while (slots.size() < stacks.size()) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new FortressNotInsertableSlot(this.screenInventory, column + clientCurrentRow * 9, 8 + column * 18, 80 + clientCurrentRow * 18));
            }
            clientCurrentRow++;
        }
        super.updateSlotStacks(revision, stacks, cursorStack);
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        return index != this.getCraftingResultSlotIndex();
    }

    protected void createDefaultsScrollableSlots() {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new FortressNotInsertableSlot(this.screenInventory, column + row * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new FortressNotInsertableSlot(this.screenInventory, column + 27, 8 + column * 18, 142));
        }

        if(this.serverResourceManager != null) {
            int rowsCount = getRowsCount();
            if(rowsCount > 4) {
                for (int row = 0; row < rowsCount-4; row++) {
                    for (int column = 0; column < 9; ++column) {
                        this.addSlot(new FortressNotInsertableSlot(this.screenInventory, column + (row+4) * 9, 8 + column * 18, 150 + row * 18));
                    }
                }
            }

            this.scrollItems(0f);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }



    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            final var stack = slot.getStack();
            ItemStack itemStack2 = new FortressItemStack(stack.getItem(), stack.getCount());
            itemStack = itemStack2.copy();
            if (index == 0) {
                if (!this.insertItem(itemStack2, 10, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(itemStack2, itemStack);
            } else if (index >= 10 && index < 46 ? !this.insertItem(itemStack2, 1, 10, false) && (index < 37 ? !this.insertItem(itemStack2, 37, 46, false) : !this.insertItem(itemStack2, 10, 37, false)) : !this.insertItem(itemStack2, 10, 46, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setStackNoCallbacks(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, itemStack2);
            if (index == 0) {
                player.dropItem(itemStack2, false);
            }
        }
        return itemStack;
    }

    public SimpleInventory getScreenInventory() {
        return screenInventory;
    }


    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if(player instanceof ServerPlayerEntity serverPlayer && serverPlayer.server instanceof FortressServer fortressServer) {
            final var fortressServerManager = fortressServer.get_FortressModServerManager().getByPlayer(serverPlayer);
            final var serverResourceManager = fortressServerManager.getServerResourceManager();

            returnInputs();

            final var diff = this.virtualInventory.getDiff();
            diff.added.forEach(stack -> serverResourceManager.increaseItemAmount(stack.item(), stack.amount()));
            diff.updated.forEach(stack -> serverResourceManager.increaseItemAmount(stack.item(), stack.amount()));
            diff.removed.forEach(stack -> serverResourceManager.increaseItemAmount(stack.item(), -stack.amount()));
        }
    }

    protected void returnInputs() {
        new FortressInputSlotFiller(this).returnInputs();
    }


    @SuppressWarnings("unchecked")
    @Override
    public void fillInputSlots(boolean craftAll, Recipe<?> recipe, ServerPlayerEntity player) {
        new FortressInputSlotFiller(this).fillInputSlots(player, (Recipe<CraftingInventory>) recipe, craftAll);
    }

    protected abstract T getInput();

    @Override
    public void scrollItems(float position) {
        if(serverResourceManager == null) {
            final var packet = new ServerboundScrollCurrentScreenPacket(position);
            FortressClientNetworkHelper.send(FortressChannelNames.SCROLL_CURRENT_SCREEN, packet);
            return;
        }

        final var totalRows = getRowsCount();
        int totalAdditionalRows = totalRows - 4;
        int rowOffset = (int)((double)(position * (float)totalAdditionalRows) + 0.5);
        if (rowOffset < 0) {
            rowOffset = 0;
        }
        for (int row = 0; row < totalRows; ++row) {
            for (int column = 0; column < 9; ++column) {
                var currentRow = row + rowOffset;
                if (currentRow >= totalRows) {
                    currentRow -= totalRows;
                }
                int m = column + currentRow * 9;
                if (m >= 0 && m < this.virtualInventory.size()) {
                    screenInventory.setStack(column + row * 9, virtualInventory.get(m));
                    continue;
                }
                screenInventory.setStack(column + row * 9, ItemStack.EMPTY);
            }
        }

        this.virtualInventory.setRowsOffset(rowOffset);
    }

    public VirtualInventory getVirtualInventory() {
        return virtualInventory;
    }

    @Override
    public boolean matches(Recipe<? super T> recipe) {
        return recipe.matches(getInput(), world);
    }

    protected static class FortressSlot extends Slot {

        public FortressSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getMaxItemCount(ItemStack stack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getMaxItemCount() {
            return Integer.MAX_VALUE;
        }
    }

    protected final class FortressNotInsertableSlot extends FortressSlot {

        public FortressNotInsertableSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public void setStackNoCallbacks(ItemStack stack) {
            final var handler = AbstractFortressRecipeScreenHandler.this;
            if(handler.virtualInventory != null)
                handler.virtualInventory.set(this.getIndex(), stack);
            super.setStackNoCallbacks(stack);
        }

        @Override
        public ItemStack getStack() {
            return super.getStack();
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return this.getStack().isEmpty() || ItemStack.canCombine(this.getStack(), stack);
        }

        @Override
        public ItemStack insertStack(ItemStack stack, int count) {
            if (stack.isEmpty() || !this.canInsert(stack)) {
                return stack;
            }
            ItemStack itemStack = this.getStack();
            if (itemStack.isEmpty()) {
                if(this.inventory instanceof FortressSimpleInventory fortressSimpleInventory) {
                    final var i = fortressSimpleInventory.indexOf(stack);
                    if(i != -1) {
                        this.inventory.getStack(i).increment(count);
                        stack.decrement(count);
                    } else {
                        final var split = stack.split(count);
                        this.setStackNoCallbacks(split);
                    }
                }
            } else if (ItemStack.canCombine(itemStack, stack)) {
                itemStack.increment(count);
                this.setStackNoCallbacks(itemStack);
                stack.decrement(count);
            }
            return stack;
        }
    }

    protected final class VirtualInventory {

        private final Map<Item, Integer> itemsBefore;
        private final List<ItemStack> items;

        private int rowsOffset = 0;

        VirtualInventory(List<ItemStack> items) {
            this.items = new ArrayList<>(items.stream().filter(it -> !it.isEmpty()).toList());
            this.itemsBefore = this.items
                    .stream()
                    .collect(Collectors.collectingAndThen(Collectors.toMap(ItemStack::getItem, ItemStack::getCount), Collections::unmodifiableMap));
        }

        boolean full() {
            return this.items.stream().noneMatch(ItemStack::isEmpty);
        }

        ItemStack get(int index) {
            return this.items.get(index);
        }

        boolean hasItem(Item item) {
            return this.items.stream().anyMatch(it -> it.getItem().equals(item));
        }

        void addItem(ItemStack itemStack) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).isEmpty()) {
                    items.set(i, itemStack);
                    return;
                }
            }
        }

        void set(int index, ItemStack stack) {
            final var slots = AbstractFortressRecipeScreenHandler.this.slots;

            final var storageSlotsCount = slots.size() - AbstractFortressRecipeScreenHandler.this.getCraftingSlotCount();
            while (storageSlotsCount>items.size()) {
                this.items.add(ItemStack.EMPTY);
            }

            final var realIndex = index + rowsOffset * 9;
            final var itemsCount = this.items.size();

            final var insertIndex = realIndex < itemsCount ? realIndex : (realIndex - itemsCount);

            if(this.items.get(insertIndex).isEmpty() || stack.isEmpty() || this.items.get(insertIndex).equals(stack))
                this.items.set(insertIndex, stack);
            else
                throw new IllegalStateException();
        }

        void setRowsOffset(int rowsOffset) {
            this.rowsOffset = rowsOffset;
        }

        int size() {
            return this.items.size();
        }

        InventoryDiff getDiff() {
            final var itemsAfter = this.items.stream()
                    .filter(it -> !it.isEmpty())
                    .map(ItemStack::getItem)
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));

            final var itemsToRemove = itemsBefore.entrySet()
                    .stream()
                    .filter(it -> !itemsAfter.contains(it.getKey()))
                    .map(it -> new ItemInfo(it.getKey(), it.getValue()))
                    .toList();

            final var itemsToAdd = items.stream()
                    .filter(it -> !it.isEmpty())
                    .filter(it -> !itemsBefore.containsKey(it.getItem()))
                    .map(it -> new ItemInfo(it.getItem(), it.getCount()))
                    .toList();

            final var itemsToUpdate = items.stream()
                    .filter(it -> !it.isEmpty())
                    .filter(it -> itemsBefore.containsKey(it.getItem()))
                    .map(it -> new ItemInfo(it.getItem(), it.getCount() - itemsBefore.get(it.getItem())))
                    .toList();

            return new InventoryDiff(itemsToAdd, itemsToUpdate, itemsToRemove);
        }

    }

    protected record InventoryDiff(List<ItemInfo> added, List<ItemInfo> updated, List<ItemInfo> removed) {}

}
