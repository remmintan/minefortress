package org.minefortress.mixins.renderer.gui;

import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.world.GameMode;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import org.minefortress.MineFortressClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameModeSelectionScreen.GameModeSelection.class)
public abstract class GameModeSelectionScreenMixin {

    @Inject(method = "of", at = @At("HEAD"), cancellable = true)
    private static void of(GameMode gameMode, CallbackInfoReturnable<GameModeSelectionScreen.GameModeSelection> cir) {
        if (gameMode == FortressGamemodeUtilsKt.getFORTRESS()) {
            cir.setReturnValue(MineFortressClient.FORTRESS_SELECTION);
        }
    }

    // next
    @Inject(method = "next", at = @At("HEAD"), cancellable = true)
    private void next(CallbackInfoReturnable<GameModeSelectionScreen.GameModeSelection> cir) {
        if((Object)this == GameModeSelectionScreen.GameModeSelection.SPECTATOR) {
            cir.setReturnValue(MineFortressClient.FORTRESS_SELECTION);
        }
        if((Object)this == MineFortressClient.FORTRESS_SELECTION) {
            cir.setReturnValue(GameModeSelectionScreen.GameModeSelection.CREATIVE);
        }
    }

}
