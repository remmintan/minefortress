package org.minefortress.mixins.renderer.gui.worldcreator;

import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.world.Difficulty;
import org.minefortress.interfaces.FortressWorldCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldCreator.class)
public abstract class WorldCreatorMixin implements FortressWorldCreator {

    @Unique
    private boolean borderEnabled = true;
    @Unique
    private boolean showCampfire = true;

    @Shadow public abstract void setDifficulty(Difficulty difficulty);

    @Shadow public abstract void setCheatsEnabled(boolean cheatsEnabled);

    @Inject(method = "setGameMode", at = @At("HEAD"))
    public void setGameMode(WorldCreator.Mode gameMode, CallbackInfo ci) {
        if(gameMode == WorldCreator.Mode.DEBUG) {
            this.setDifficulty(Difficulty.PEACEFUL);
            this.setCheatsEnabled(true);
        }
    }

    @Override
    public boolean is_BorderEnabled() {
        return borderEnabled;
    }

    @Override
    public void set_BorderEnabled(boolean borderEnabled) {
        this.borderEnabled = borderEnabled;
    }

    @Override
    public boolean is_ShowCampfire() {
        return showCampfire;
    }

    @Override
    public void set_ShowCampfire(boolean showCampfire) {
        this.showCampfire = showCampfire;
    }
}
