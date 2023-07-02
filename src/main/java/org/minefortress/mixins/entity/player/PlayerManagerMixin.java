package org.minefortress.mixins.entity.player;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;refreshPositionAndAngles(DDDFF)V"))
    public void respawnPlayer(ServerPlayerEntity instance, double x, double y, double z, float yaw, float pitch) {
        if(ModUtils.isFortressGamemode(instance))
            instance.refreshPositionAndAngles(x, y, z, 45, 60);
    }
}
