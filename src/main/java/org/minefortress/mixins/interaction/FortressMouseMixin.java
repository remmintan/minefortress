package org.minefortress.mixins.interaction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class FortressMouseMixin {

    private int fortressControlledLeftClicks;
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow private double x;

    @Shadow private double y;

    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    public void lockCursor(CallbackInfo ci) {
        if (ModUtils.isClientInFortressGamemode() && !ModUtils.shouldReleaseCamera()) {
            ci.cancel();
        }
    }

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    public void updateMouse(CallbackInfo ci) {
        if(((FortressMinecraftClient) this.client).is_FortressGamemode() && !ModUtils.shouldReleaseCamera()) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
        if(!fortressClient.is_FortressGamemode()) {
            return;
        }

        double mouseX = this.x * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth();
        double mouseY = this.y * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight();
        boolean isPress = action == 1;

        if (MinecraftClient.IS_SYSTEM_MAC && button == 0) {
            if (isPress) {
                if ((mods & 2) == 2) {
                    button = 1;
                    ++this.fortressControlledLeftClicks;
                }
            } else if (this.fortressControlledLeftClicks > 0) {
                button = 1;
                --this.fortressControlledLeftClicks;
            }
        }

        if(isPress && button == 0 && client.currentScreen == null) {
            fortressClient.get_FortressHud().onClick(mouseX, mouseY);
        }

    }

}
