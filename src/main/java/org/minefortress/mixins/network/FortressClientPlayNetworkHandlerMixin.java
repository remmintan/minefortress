package org.minefortress.mixins.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.telemetry.TelemetryManager;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.world.GameMode;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class FortressClientPlayNetworkHandlerMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow @Final private TelemetryManager telemetrySender;

    @Inject(method = "onPlayerRespawn", at = @At("TAIL"))
    public void onPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        if (packet.getDimension() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY && this.client.interactionManager != null)
            this.client.interactionManager.setGameMode(GameMode.CREATIVE);
    }

    @Inject(method = "onGameJoin", at = @At(value = "TAIL", shift = At.Shift.BY, by = -1), cancellable = true)
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        final GameMode gameMode = packet.gameMode();
        if(ModUtils.isFortressGamemode(gameMode)) {
            this.telemetrySender.setGameModeAndSend(GameMode.CREATIVE, false);
            ci.cancel();
        }
    }

}
