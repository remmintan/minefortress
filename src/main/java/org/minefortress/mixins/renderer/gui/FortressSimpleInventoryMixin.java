package org.minefortress.mixins.renderer.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.util.collection.DefaultedList;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import org.minefortress.interfaces.FortressSimpleInventory;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SimpleInventory.class)
public abstract class FortressSimpleInventoryMixin implements FortressSimpleInventory {

    @Shadow
    @Final
    public DefaultedList<ItemStack> stacks;
    @Unique
    private int changeCount = 0;

    @Override
    public int getMaxCountPerStack() {
        final var clientFortressSurvival = FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER && FortressGamemodeUtilsKt.isClientInFortressGamemode() && !ModUtils.getFortressClientManager().isCreative();
        if(clientFortressSurvival || FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
            return Integer.MAX_VALUE;
        else
            return FortressSimpleInventory.super.getMaxCountPerStack();
    }

    @Override
    public List<ItemStack> get_Stacks() {
        return this.stacks;
    }

    @Override
    public int get_OccupiedSlotWithRoomForStack(ItemStack stack) {
        for (int i = 0; i < this.stacks.size(); i++) {
            ItemStack itemStack = this.stacks.get(i);
            if(ItemStack.canCombine(itemStack, stack))
                return i;
        }
        return -1;
    }

    @Override
    public int index_Of(ItemStack stack) {
        if(stack == null || stack.isEmpty()) return -1;
        return this.stacks.stream()
                .filter(it -> it.getItem() == stack.getItem())
                .findFirst()
                .map(this.stacks::indexOf)
                .orElse(-1);
    }

    @Override
    public void populate_RecipeFinder(RecipeMatcher recipeMatcher) {
        for (ItemStack itemStack : this.stacks) {
            recipeMatcher.addInput(itemStack, Integer.MAX_VALUE);
        }
    }

    @Override
    public int get_ChangeCount() {
        return this.changeCount;
    }

    @Inject(method = "markDirty", at = @At("RETURN"))
    public void markDirty(CallbackInfo ci) {
        changeCount++;
    }

}
