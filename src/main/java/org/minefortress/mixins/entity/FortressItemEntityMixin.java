package org.minefortress.mixins.entity;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class FortressItemEntityMixin extends Entity {

    @Shadow private int itemAge;

    public FortressItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;getStack()Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE), cancellable = true)
    void disablePickup(PlayerEntity player, CallbackInfo ci) {
        final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        final ServerPlayerInteractionManager interactionManager = serverPlayer.interactionManager;
        final boolean notBlueprintsWorld = player.world.getRegistryKey() != BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY;
        final boolean isFortressGamemode = interactionManager.getGameMode() == ClassTinkerers.getEnum(GameMode.class, "FORTRESS");
        if(isFortressGamemode && notBlueprintsWorld) {
            ci.cancel();
        }
    }

    @Inject(method = "getItemAge", at = @At("HEAD"), cancellable = true)
    void getItemAge(CallbackInfoReturnable<Integer> cir) {
        if(this.world.getRegistryKey() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY){
            cir.setReturnValue(6000);
        } else {
            cir.setReturnValue(this.itemAge);
        }
    }

}
