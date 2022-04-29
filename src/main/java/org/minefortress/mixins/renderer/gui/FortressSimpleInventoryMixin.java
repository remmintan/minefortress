package org.minefortress.mixins.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.util.collection.DefaultedList;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.interfaces.FortressSimpleInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SimpleInventory.class)
public abstract class FortressSimpleInventoryMixin implements FortressSimpleInventory {

    private int changeCount = 0;

    @Shadow @Final private DefaultedList<ItemStack> stacks;

    @Override
    public int getMaxCountPerStack() {
        if(getFortressMinecraftClient().isFortressGamemode() && isNotCreative())
            return 10000;
        else
            return FortressSimpleInventory.super.getMaxCountPerStack();
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
        if(stack == null || stack.isEmpty()) return -1;
        return this.stacks.stream()
                .filter(it -> it.getItem() == stack.getItem())
                .findFirst()
                .map(this.stacks::indexOf)
                .orElse(-1);
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher recipeMatcher) {
        for (ItemStack itemStack : this.stacks) {
            recipeMatcher.addInput(itemStack, 10000);
        }
    }

    @Override
    public int getChangeCount() {
        return this.changeCount;
    }

    @Inject(method = "markDirty", at = @At("RETURN"))
    public void markDirty(CallbackInfo ci) {
        changeCount++;
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
