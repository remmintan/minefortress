package org.minefortress.mixins.renderer.gui.worldcreator;

import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.world.Difficulty;
import org.minefortress.interfaces.FortressWorldCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldCreator.class)
public abstract class WorldCreatorMixin implements FortressWorldCreator {

    @Shadow public abstract void setDifficulty(Difficulty difficulty);

    @Shadow public abstract void setCheatsEnabled(boolean cheatsEnabled);

    @Inject(method = "setGameMode", at = @At("HEAD"))
    public void setGameMode(WorldCreator.Mode gameMode, CallbackInfo ci) {
        if(gameMode == WorldCreator.Mode.DEBUG) {
            this.setDifficulty(Difficulty.PEACEFUL);
            this.setCheatsEnabled(true);
        }
    }

}
