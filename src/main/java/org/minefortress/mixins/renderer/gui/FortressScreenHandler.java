package org.minefortress.mixins.renderer.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.Set;

@Mixin(ScreenHandler.class)
public abstract class FortressScreenHandler {

    private static final Set<SlotActionType> FORBIDDEN_SLOT_ACTIONS = EnumSet.of(SlotActionType.CLONE, SlotActionType.SWAP);

    @Inject(method = "internalOnSlotClick", at = @At(value = "HEAD"), cancellable = true)
    void internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if(ModUtils.isFortressGamemode(player) && FORBIDDEN_SLOT_ACTIONS.contains(actionType)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "internalOnSlotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    void internalOnSlotClickDrop(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if(ModUtils.isFortressGamemode(player)) {
            ci.cancel();
        }
    }

}
