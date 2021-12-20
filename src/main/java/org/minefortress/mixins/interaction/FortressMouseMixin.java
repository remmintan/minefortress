package org.minefortress.mixins.interaction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class FortressMouseMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    public void lockCursor(CallbackInfo ci) {
        FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        final boolean fortressGamemode = !fortressClient.isNotFortressGamemode();
        final boolean middleMouseNotPressed = isMiddleMouseNotPressed();
        if (fortressGamemode && middleMouseNotPressed) {
            ci.cancel();
        }
    }

    private boolean isMiddleMouseNotPressed() {
        return !client.options.keyPickItem.isPressed();
    }

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    public void updateMouse(CallbackInfo ci) {
        if(((FortressMinecraftClient) this.client).isFortressGamemode() && isMiddleMouseNotPressed()) {
            ci.cancel();
        }
    }

}
