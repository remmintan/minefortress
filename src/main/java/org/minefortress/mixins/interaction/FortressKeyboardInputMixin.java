package org.minefortress.mixins.interaction;

import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class FortressKeyboardInputMixin extends Input {

    @Shadow @Final private GameOptions settings;

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(boolean slowDown, CallbackInfo ci) {
        if(this.settings.keySprint.isPressed()) {
            this.movementForward = 0.0f;
            this.movementSideways = 0.0f;
        }
    }

}
