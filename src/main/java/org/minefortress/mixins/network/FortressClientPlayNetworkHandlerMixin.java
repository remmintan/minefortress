package org.minefortress.mixins.network;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.telemetry.TelemetrySender;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class FortressClientPlayNetworkHandlerMixin {

    @Shadow @Final private TelemetrySender telemetrySender;
    private static final GameMode FORTRESS = ClassTinkerers.getEnum(GameMode.class, "FORTRESS");

    @Inject(method = "onGameJoin", at = @At(value = "TAIL", shift = At.Shift.BY, by = -1), cancellable = true)
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        final GameMode gameMode = packet.gameMode();
        if(gameMode == FORTRESS) {
            this.telemetrySender.setGameModeAndSend(GameMode.CREATIVE, false);
            ci.cancel();
        }
    }

}
