package org.minefortress.mixins.interaction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import org.minefortress.interfaces.IFortressMinecraftClient;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class FortressMouseMixin {

    @Unique
    private int fortressControlledLeftClicks;
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow private double x;

    @Shadow private double y;

    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    public void lockCursor(CallbackInfo ci) {
        if (FortressGamemodeUtilsKt.isClientInFortressGamemode() && !ModUtils.shouldReleaseCamera()) {
            ci.cancel();
        }
    }

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    public void updateMouse(CallbackInfo ci) {
        if (FortressGamemodeUtilsKt.isClientInFortressGamemode() && !ModUtils.shouldReleaseCamera()) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        final IFortressMinecraftClient fortressClient = (IFortressMinecraftClient) this.client;
        if (!FortressGamemodeUtilsKt.isClientInFortressGamemode()) {
            return;
        }

        double mouseX = this.x * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
        double mouseY = this.y * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();

        int effectiveButton = button;
        if (MinecraftClient.IS_SYSTEM_MAC && button == 0) { // left click
            if (action == 1) { // press
                if ((mods & 2) == 2) { // control down
                    effectiveButton = 1; // treat as right click
                    ++this.fortressControlledLeftClicks;
                }
            } else if (this.fortressControlledLeftClicks > 0) { // release
                effectiveButton = 1; // treat as right click
                --this.fortressControlledLeftClicks;
            }
        }

        if (client.currentScreen == null) { // Only interact with HUD if no screen is open
            if (action == 1) { // Press
                fortressClient.get_FortressHud().onPress(mouseX, mouseY);
            } else if (action == 0 && effectiveButton == 0) { // Release (only for original left click)
                fortressClient.get_FortressHud().onRelease(mouseX, mouseY);
            }
        }
    }

}