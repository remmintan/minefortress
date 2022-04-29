package org.minefortress.mixins.entity;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class FortressItemEntityMixin extends Entity {

    @Shadow private int itemAge;

    @Shadow public abstract ItemStack getStack();

    public FortressItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;getStack()Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE), cancellable = true)
    void disablePickup(PlayerEntity player, CallbackInfo ci) {
        final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        final ServerPlayerInteractionManager interactionManager = serverPlayer.interactionManager;
        final boolean notBlueprintsWorld = !isBlueprintsWorld();
        final boolean isFortressGamemode = isFortressGamemode(interactionManager);
        if(isFortressGamemode && notBlueprintsWorld) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at =@At("RETURN"))
    void tickReturn(CallbackInfo ci) {
        if(isBlueprintsWorld()) {
            this.discard();
            return;
        }

        if(!this.world.isClient) {
            final var closestPlayer = this.world.getClosestPlayer(this, 100.0D);
            if(closestPlayer != null) {
                final var fortressServerPlayer = (FortressServerPlayerEntity) closestPlayer;
                if(fortressServerPlayer.isFortressSurvival()) {
                    final var fortressServerManager = fortressServerPlayer.getFortressServerManager();
                    final var resourceManager = fortressServerManager.getServerResourceManager();
                    final var stack = this.getStack();
                    resourceManager.increaseItemAmount(stack.getItem(), stack.getCount());
                    this.discard();
                }
            }
        }
    }

    private boolean isFortressGamemode(ServerPlayerInteractionManager interactionManager) {
        return interactionManager.getGameMode() == ClassTinkerers.getEnum(GameMode.class, "FORTRESS");
    }

    private boolean isBlueprintsWorld() {
        return this.world.getRegistryKey() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY;
    }

}
