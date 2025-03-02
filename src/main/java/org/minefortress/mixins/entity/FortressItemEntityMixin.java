package org.minefortress.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BlueprintsDimensionUtilsKt;
import net.remmintan.mods.minefortress.core.interfaces.professions.IServerProfessionsManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.minefortress.professions.ProfessionManager.FISHERMAN_ITEMS;
import static org.minefortress.professions.ProfessionManager.FORESTER_ITEMS;

@Mixin(ItemEntity.class)
public abstract class FortressItemEntityMixin extends Entity {

    @Shadow public abstract ItemStack getStack();

    public FortressItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at =@At("RETURN"))
    void tickReturn(CallbackInfo ci) {
        if(isBlueprintsWorld()) {
            this.discard();
            return;
        }

        if(!this.getWorld().isClient) {
            final var closestPlayer = this.getWorld().getClosestPlayer(this, 100.0D);
            if (closestPlayer != null && FortressGamemodeUtilsKt.isFortressGamemode(closestPlayer)) {
                final var fortressServer = (IFortressServer) closestPlayer.getServer();
                if(fortressServer != null) {
                    final var closestSPE = (ServerPlayerEntity) closestPlayer;
                    final var provider = ServerModUtils.getManagersProvider(closestSPE);
                    if (ServerExtensionsKt.isSurvivalFortress(closestSPE.server)) {
                        final var resourceManager = provider.getResourceManager();
                        final var stack = this.getStack();
                        final var item = stack.getItem();
                        if(shouldCollectInInventory(provider.getProfessionsManager(), item))
                            resourceManager.increaseItemAmount(item, stack.getCount());
                    }
                    this.discard();
                }
            }
        }
    }

    @Unique
    private boolean shouldCollectInInventory(IServerProfessionsManager serverProfessionManager, Item item) {
        if(item.getDefaultStack().isIn(ItemTags.SAPLINGS))
            return serverProfessionManager.hasProfession("forester");

        if(FORESTER_ITEMS.contains(item))
            return serverProfessionManager.hasProfession("forester") || serverProfessionManager.hasProfession("farmer");

        if(FISHERMAN_ITEMS.contains(item))
            return serverProfessionManager.hasProfession("fisherman");

        return true;
    }

    @Unique
    private boolean isBlueprintsWorld() {
        return this.getWorld().getRegistryKey() == BlueprintsDimensionUtilsKt.getBLUEPRINT_DIMENSION_KEY();
    }

}
