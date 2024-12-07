package org.minefortress.mixins.world;

import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import net.minecraft.client.session.telemetry.WorldLoadedEvent;
import net.minecraft.world.GameMode;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldLoadedEvent.class)
public abstract class WorldLoadedEventMixin {

    @Shadow @Nullable private TelemetryEventProperty.@Nullable GameMode gameMode;

    @Inject(method = "setGameMode", at = @At("HEAD"), cancellable = true)
    void setGameMode(GameMode gameMode, boolean hardcore, CallbackInfo ci){
        if (gameMode == FortressGamemodeUtilsKt.getFORTRESS()) {
            this.gameMode = TelemetryEventProperty.GameMode.SURVIVAL;
            ci.cancel();
        }
    }

}
