package org.minefortress.mixins.renderer.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class FortressScreenHandler {

    @Inject(method = "internalOnSlotClick", at = @At(value = "HEAD"), cancellable = true)
    void internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if(ModUtils.isFortressGamemode(player) && actionType == SlotActionType.CLONE) {
            ci.cancel();
        }
    }

}
