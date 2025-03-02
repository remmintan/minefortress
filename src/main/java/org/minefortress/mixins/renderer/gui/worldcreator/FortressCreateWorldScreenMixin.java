package org.minefortress.mixins.renderer.gui.worldcreator;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.text.Text;
import net.minecraft.world.SaveProperties;
import net.remmintan.mods.minefortress.core.interfaces.IFortressGamemodeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = "createLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServerLoader;tryLoad(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;Z)V"))
    private void tryLoad(MinecraftClient client, CreateWorldScreen parent, Lifecycle lifecycle, Runnable loader, boolean bypassWarnings) {
        // bypassing the warning screen
        IntegratedServerLoader.tryLoad(client, parent, lifecycle, loader, true);
    }

    @ModifyArg(method = "startServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServerLoader;start(Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/server/DataPackContents;Lnet/minecraft/registry/CombinedDynamicRegistries;Lnet/minecraft/world/SaveProperties;)V"))
    public SaveProperties updateLevelPropsBeforeStartingAServer(SaveProperties props) {
        final var worldCreator = this.getWorldCreator();
        if (props instanceof IFortressGamemodeHolder wcProps && worldCreator instanceof IFortressGamemodeHolder creator) {
            wcProps.set_fortressGamemode(creator.get_fortressGamemode());
        }

        return props;
    }

}
