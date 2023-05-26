package org.minefortress.mixins.entity;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

//    @Inject(
//            method = "respawnPlayer",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
//                    ordinal = 1,
//                    shift = At.Shift.BEFORE
//            )
//    )
//    public void respawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
//        player.setYaw(45);
//        player.setPitch(60);
//    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;refreshPositionAndAngles(DDDFF)V"))
    public void respawnPlayer(ServerPlayerEntity instance, double x, double y, double z, float yaw, float pitch) {
        if(ModUtils.isFortressGamemode(instance))
            instance.refreshPositionAndAngles(x, y, z, 45, 60);
    }
}
