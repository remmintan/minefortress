package org.minefortress.mixins.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.interfaces.FortressSimpleInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(SimpleInventory.class)
public abstract class FortressSimpleInventoryMixin implements Inventory, FortressSimpleInventory {

    @Shadow @Final private DefaultedList<ItemStack> stacks;

    @Override
    public int getMaxCountPerStack() {
        if(getFortressMinecraftClient().isFortressGamemode() && isNotCreative())
            return 10000;
        else
            return Inventory.super.getMaxCountPerStack();
    }

    @Override
    public List<ItemStack> getStacks() {
        return this.stacks;
    }

    @Override
    public int getOccupiedSlotWithRoomForStack(ItemStack stack) {
        for (int i = 0; i < this.stacks.size(); i++) {
            ItemStack itemStack = this.stacks.get(i);
            if(ItemStack.canCombine(itemStack, stack))
                return i;
        }
        return -1;
    }

    @Override
    public int indexOf(ItemStack stack) {
        return this.stacks.indexOf(stack);
    }

    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    private FortressClientManager getClientManager() {
        return getFortressMinecraftClient().getFortressClientManager();
    }

    private FortressMinecraftClient getFortressMinecraftClient() {
        return (FortressMinecraftClient) getClient();
    }

    private boolean isNotCreative() {
        return !getClientManager().isCreative();
    }

}
