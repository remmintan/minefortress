package org.minefortress.mixins.world;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.minefortress.interfaces.FortressServerWorld;
import org.minefortress.tasks.TaskManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class FortressServerWorldMixin extends World implements FortressServerWorld {

    private final TaskManager taskManager = new TaskManager();

    protected FortressServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
    }

    @Override
    public TaskManager getTaskManager() {
        return taskManager;
    }
}
