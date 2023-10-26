package org.minefortress.mixins;

import com.mojang.datafixers.DataFixer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ApiServices;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.level.storage.LevelStorage;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.fortress.server.FortressModServerManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class IFortressServerMixin extends ReentrantThreadExecutor<ServerTask> implements IFortressServer {

    @Shadow private Profiler profiler;
    @Shadow private int ticks;
    @Shadow private PlayerManager playerManager;
    @Shadow @Final private WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;
    @Shadow protected abstract boolean shouldKeepTicking();

    @Unique
    private BlueprintsWorld blueprintsWorld;
    @Unique
    private FortressModServerManager fortressModServerManager;

    public IFortressServerMixin(String string) {
        super(string);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        blueprintsWorld = new BlueprintsWorld((MinecraftServer) (Object)this);
        fortressModServerManager = new FortressModServerManager((MinecraftServer)(Object)this);
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
        if(this.shouldKeepTicking() && blueprintsWorld.hasWorld()) {
            final var world = blueprintsWorld.getWorld();
            final boolean executed = world instanceof ServerWorld w && w.getChunkManager().executeQueuedTasks();
            if(executed) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public BlueprintsWorld get_BlueprintsWorld() {
        return blueprintsWorld;
    }

    @Override
    public WorldGenerationProgressListener get_WorldGenerationProgressListener() {
        return this.worldGenerationProgressListenerFactory.create(11);
    }

    @Override
    public FortressModServerManager get_FortressModServerManager() {
        return fortressModServerManager;
    }
}
