package org.minefortress.mixins.entity;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.tag.ItemTags;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.minefortress.MineFortressMod;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.resources.SimilarItemsHelper;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.professions.ServerProfessionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

import static org.minefortress.professions.ProfessionManager.FISHERMAN_ITEMS;
import static org.minefortress.professions.ProfessionManager.FORESTER_ITEMS;

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
                    final var item = stack.getItem();
                    if(shouldCollectInInventory(fortressServerManager.getServerProfessionManager(), item))
                        resourceManager.increaseItemAmount(item, stack.getCount());

                    this.discard();
                }
            }

        }
    }

    private boolean shouldCollectInInventory(ServerProfessionManager serverProfessionManager, Item item) {
        if(SimilarItemsHelper.contains(ItemTags.SAPLINGS, item))
            return serverProfessionManager.hasProfession("forester");

        if(FORESTER_ITEMS.contains(item))
            return serverProfessionManager.hasProfession("forester") || serverProfessionManager.hasProfession("farmer");

        if(FISHERMAN_ITEMS.contains(item))
            return serverProfessionManager.hasProfession("fisherman");

        return true;
    }

    private boolean isFortressGamemode(ServerPlayerInteractionManager interactionManager) {
        return interactionManager.getGameMode() == MineFortressMod.FORTRESS;
    }

    private boolean isBlueprintsWorld() {
        return this.world.getRegistryKey() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY;
    }

}
