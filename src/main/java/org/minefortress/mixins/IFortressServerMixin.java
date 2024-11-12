package org.minefortress.mixins;

import com.mojang.datafixers.DataFixer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ApiServices;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintWorld;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.world.BlueprintWorldWrapper;
import org.minefortress.fortress.server.FortressModServerManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(MinecraftServer.class)
public abstract class IFortressServerMixin extends ReentrantThreadExecutor<ServerTask> implements IFortressServer {

    @Shadow @Final private WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;

    @Shadow
    @Nullable
    public abstract ServerWorld getWorld(RegistryKey<World> key);

    @Unique
    private FortressModServerManager fortressModServerManager;

    public IFortressServerMixin(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        fortressModServerManager = new FortressModServerManager((MinecraftServer)(Object)this);
    }

    @Override
    public WorldGenerationProgressListener get_WorldGenerationProgressListener() {
        return this.worldGenerationProgressListenerFactory.create(11);
    }

    @Override
    public FortressModServerManager get_FortressModServerManager() {
        return fortressModServerManager;
    }

    @Override
    public IBlueprintWorld get_BlueprintWorld() {
        return new BlueprintWorldWrapper((MinecraftServer) (Object) this);
    }
}
