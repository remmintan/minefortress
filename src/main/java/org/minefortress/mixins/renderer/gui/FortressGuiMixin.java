package org.minefortress.mixins.renderer.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import net.remmintan.mods.minefortress.core.FortressState;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class FortressGuiMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair(DrawContext context, CallbackInfo ci) {
        if (FortressGamemodeUtilsKt.isClientInFortressGamemode()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void renderHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
        if (FortressGamemodeUtilsKt.isClientInFortressGamemode() && ModUtils.getFortressClientManager().getState() == FortressState.COMBAT) {
            ci.cancel();
        }
    }

}
