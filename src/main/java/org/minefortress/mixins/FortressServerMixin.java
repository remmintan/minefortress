package org.minefortress.mixins;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.*;
import net.minecraft.util.UserCache;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.interfaces.FortressServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class FortressServerMixin extends ReentrantThreadExecutor<ServerTask> implements FortressServer {

    @Shadow private Profiler profiler;
    @Shadow private int ticks;
    @Shadow private PlayerManager playerManager;

    @Shadow @Final private WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;

    @Shadow protected abstract boolean shouldKeepTicking();

    private BlueprintsWorld blueprintsWorld;

    public FortressServerMixin(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void init(Thread serverThread, DynamicRegistryManager.Impl registryManager, LevelStorage.Session session, SaveProperties saveProperties, ResourcePackManager dataPackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, MinecraftSessionService sessionService, GameProfileRepository gameProfileRepo, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        blueprintsWorld = new BlueprintsWorld((MinecraftServer) (Object)this);
    }

    @Inject(method = "tickWorlds", at = @At(value="INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.BEFORE))
    public void tickWorlds(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.profiler.push(() -> "Blueprint Edit world");
        if (this.ticks % 20 == 0) {
            this.profiler.push("timeSync");
            blueprintsWorld.sendToDimension(this.playerManager);
            this.profiler.pop();
        }
        this.profiler.push("tick");
        try {
            blueprintsWorld.tick(shouldKeepTicking);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, "Exception ticking blueprint world");
            throw new CrashException(crashReport);
        }
        this.profiler.pop();
        this.profiler.pop();
    }

    @Inject(method = "runOneTask", at = @At(value = "TAIL", shift = At.Shift.BEFORE), cancellable = true)
    public void executeOneTask(CallbackInfoReturnable<Boolean> cir) {
        if(this.shouldKeepTicking()) {
            final boolean executed = blueprintsWorld.getServerWorld().getChunkManager().executeQueuedTasks();
            if(executed) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public BlueprintsWorld getBlueprintsWorld() {
        return blueprintsWorld;
    }

    @Override
    public WorldGenerationProgressListener getWorldGenerationProgressListener() {
        return this.worldGenerationProgressListenerFactory.create(11);
    }
}
