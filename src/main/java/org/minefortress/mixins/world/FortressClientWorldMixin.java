package org.minefortress.mixins.world;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.selections.ClientTasksHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class FortressClientWorldMixin extends World implements FortressClientWorld {

    private ClientTasksHolder tasksHolder;

    protected FortressClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }


    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructorHead(ClientPlayNetworkHandler networkHandler, ClientWorld.Properties properties, RegistryKey registryRef, DimensionType dimensionType, int loadDistance, Supplier profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        tasksHolder = new ClientTasksHolder((ClientWorld) (Object)this, worldRenderer);
    }

    @Override
    public ClientTasksHolder getClientTasksHolder() {
        return tasksHolder;
    }
}
