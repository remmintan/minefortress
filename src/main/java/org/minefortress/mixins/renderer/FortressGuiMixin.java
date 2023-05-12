package org.minefortress.mixins.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.minefortress.fortress.FortressState;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class FortressGuiMixin extends DrawableHelper {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair(MatrixStack matrices, CallbackInfo ci) {
        if (((FortressMinecraftClient)client).isFortressGamemode()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void renderHotbar(float tickDelta, MatrixStack matrices, CallbackInfo ci) {
        if (ModUtils.isClientInFortressGamemode() && ModUtils.getFortressClientManager().getState() == FortressState.COMBAT) {
            ci.cancel();
        }
    }

}
