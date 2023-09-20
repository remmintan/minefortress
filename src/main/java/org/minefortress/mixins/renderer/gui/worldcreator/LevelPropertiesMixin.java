package org.minefortress.mixins.renderer.gui.worldcreator;

import net.minecraft.world.level.LevelProperties;
import org.minefortress.interfaces.FortressWorldCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelProperties.class)
public abstract class LevelPropertiesMixin implements FortressWorldCreator {

    // true by default because we need compatibility with existing worlds
    @Unique
    private boolean showCampfire = true;
    @Unique
    private boolean borderEnabled = true;

    @Override
    public void set_ShowCampfire(boolean showCampfire) {
        this.showCampfire = showCampfire;
    }

    @Override
    public boolean is_ShowCampfire() {
        return this.showCampfire;
    }

    @Override
    public void set_BorderEnabled(boolean borderEnabled) {
        this.borderEnabled = borderEnabled;
    }

    @Override
    public boolean is_BorderEnabled() {
        return this.borderEnabled;
    }
}
