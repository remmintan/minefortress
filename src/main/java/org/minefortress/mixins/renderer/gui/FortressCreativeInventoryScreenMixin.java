package org.minefortress.mixins.renderer.gui;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.resources.FortressSurvivalInventoryScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public abstract class FortressCreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {

    private static final GameMode FORTRESS_GAMEMODE = ClassTinkerers.getEnum(GameMode.class, "FORTRESS");

    public FortressCreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void init(PlayerEntity player, CallbackInfo ci) {
        if(isFortressGamemode() && !getClientManager().isCreative()){
            super.handler = new FortressSurvivalInventoryScreenHandler(player);
            player.currentScreenHandler = super.handler;
        }
    }

    @Redirect(method = "onMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;"))
    ItemEntity dropItem(ClientPlayerEntity instance, ItemStack itemStack, boolean b) {
        if(isFortressGamemode())
            return null;
        else {
            return instance.dropItem(itemStack, b);
        }
    }

    @Redirect(method = "onMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;dropCreativeStack(Lnet/minecraft/item/ItemStack;)V"))
    void dropCreativeStack(ClientPlayerInteractionManager instance, ItemStack stack) {
        if(!isFortressGamemode()) {
            instance.dropCreativeStack(stack);
        }
    }


    private boolean isFortressGamemode() {
        return getClient().interactionManager != null && FORTRESS_GAMEMODE == getClient().interactionManager.getCurrentGameMode();
    }

    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    private FortressClientManager getClientManager() {
        return ((FortressMinecraftClient) getClient()).getFortressClientManager();
    }

}
