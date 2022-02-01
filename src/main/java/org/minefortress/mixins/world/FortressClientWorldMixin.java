package org.minefortress.mixins.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.minefortress.blueprints.BlueprintManager;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.tasks.ClientTasksHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class FortressClientWorldMixin extends World implements FortressClientWorld {

    private ClientTasksHolder tasksHolder;
    private BlueprintManager blueprintManager;

    protected FortressClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }


    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructorHead(ClientPlayNetworkHandler netHandler, ClientWorld.Properties properties, RegistryKey registryRef, DimensionType dimensionType, int loadDistance, int simulationDistance, Supplier profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        tasksHolder = new ClientTasksHolder((ClientWorld) (Object)this, worldRenderer);
        blueprintManager = new BlueprintManager(MinecraftClient.getInstance());
    }

    @Override
    public ClientTasksHolder getClientTasksHolder() {
        return tasksHolder;
    }

    @Override
    public BlueprintManager getBlueprintManager() {
        return this.blueprintManager;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if(shouldKeepTicking.getAsBoolean()) {
            blueprintManager.tick();
        }
    }

}
