package org.minefortress.mixins.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.minefortress.blueprints.ServerBlueprintManager;
import org.minefortress.interfaces.FortressServerWorld;
import org.minefortress.tasks.TaskManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class FortressServerWorldMixin extends World implements FortressServerWorld {

    private final TaskManager taskManager = new TaskManager();
    private ServerBlueprintManager blueprintManager;

    protected FortressServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, CallbackInfo ci) {
        this.blueprintManager = new ServerBlueprintManager(server);
    }

    @Override
    public TaskManager getTaskManager() {
        return taskManager;
    }

    @Override
    public ServerBlueprintManager getBlueprintManager() {
        return blueprintManager;
    }
}
