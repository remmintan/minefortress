package org.minefortress.mixins.server;

import net.minecraft.server.integrated.IntegratedServerLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(IntegratedServerLoader.class)
public class FortressIntegratedServerLoaderMixin {

    @ModifyVariable(
            method = "start(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZZ)V",
            at = @At(value = "LOAD", ordinal = 1),
            ordinal = 1,
            argsOnly = true
    )
    private boolean canShowBackupPrompt(boolean lastArg) {
        return false;
    }

}
