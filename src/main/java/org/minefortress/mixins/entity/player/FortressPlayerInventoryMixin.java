package org.minefortress.mixins.entity.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(PlayerInventory.class)
public abstract class FortressPlayerInventoryMixin {

    @Shadow @Final public PlayerEntity player;

    @Inject(method = "populateRecipeFinder", at = @At("HEAD"), cancellable = true)
    void populateFinder(RecipeMatcher finder, CallbackInfo ci) {
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            final var player = (ServerPlayerEntity) this.player;
            final var allItems = ServerModUtils.getManagersProvider(player)
                    .map(IServerManagersProvider::getResourceManager)
                    .map(IServerResourceManager::getAllItems)
                    .orElse(Collections.emptyList());
            finder.clear();
            allItems.forEach(it -> finder.addInput(it, Integer.MAX_VALUE));
            ci.cancel();
        }
    }

}
