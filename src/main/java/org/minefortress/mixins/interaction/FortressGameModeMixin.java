package org.minefortress.mixins.interaction;

import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.world.GameMode;
import org.minefortress.MineFortressMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameMode.class)
public class FortressGameModeMixin {

    @Inject(method = "setAbilities", at = @At("TAIL"))
    public void setAbilities(PlayerAbilities abilities, CallbackInfo ci) {
        GameMode currentGameMode = (GameMode) (Object) this;

        if(currentGameMode == MineFortressMod.FORTRESS) {
            abilities.allowFlying = true;
            abilities.creativeMode = true;
            abilities.invulnerable = true;
        }
    }

    @Inject(method = "isCreative", at = @At("HEAD"), cancellable = true)
    public void isCreative(CallbackInfoReturnable<Boolean> cir) {
        if((Object) this == MineFortressMod.FORTRESS) {
            cir.setReturnValue(true);
        }
    }

}
