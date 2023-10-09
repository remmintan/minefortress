package org.minefortress.mixins.entity.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class FortressPlayerInventoryMixin {

    @Shadow @Final public PlayerEntity player;

    @Inject(method = "populateRecipeFinder", at = @At("HEAD"), cancellable = true)
    void populateFinder(RecipeMatcher finder, CallbackInfo ci) {;
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            final var player = (ServerPlayerEntity) this.player;
            final var server = (IFortressServer)player.getServer();
            final var serverManager = server.get_FortressModServerManager().getByPlayer(player);
            final var allItems = serverManager.getServerResourceManager().getAllItems();
            finder.clear();
            allItems.forEach(it -> finder.addInput(it, Integer.MAX_VALUE));
            ci.cancel();
        }
    }

}
