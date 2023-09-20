package org.minefortress.mixins.renderer.gui.worldcreator;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.text.Text;
import net.minecraft.world.SaveProperties;
import org.minefortress.interfaces.FortressWorldCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

    @ModifyArg(method="startServer", at=@At(value="INVOKE", target="Lnet/minecraft/server/integrated/IntegratedServerLoader;start(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/server/DataPackContents;Lnet/minecraft/registry/CombinedDynamicRegistries;Lnet/minecraft/world/SaveProperties;)V"))
    public SaveProperties updateLevelPropsBeforeStartingAServer(SaveProperties props) {
        final var worldCreator = this.getWorldCreator();
        if(props instanceof FortressWorldCreator wcProps && worldCreator instanceof FortressWorldCreator fortressWorldCreator) {
            wcProps.set_ShowCampfire(fortressWorldCreator.is_ShowCampfire());
            wcProps.set_BorderEnabled(fortressWorldCreator.is_BorderEnabled());
        }

        return props;
    }


}
