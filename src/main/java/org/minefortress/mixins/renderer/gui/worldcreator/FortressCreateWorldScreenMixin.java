package org.minefortress.mixins.renderer.gui.worldcreator;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class FortressCreateWorldScreenMixin extends Screen {

    @Shadow public abstract WorldCreator getWorldCreator();

    protected FortressCreateWorldScreenMixin(Text title) {
        super(title);
    }


    @Inject(method = "init", at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        this.getWorldCreator().setGameMode(WorldCreator.Mode.DEBUG);
    }



}
