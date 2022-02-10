package org.minefortress.blueprints.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;

import java.util.List;
import java.util.concurrent.Executor;

public class FortressServerWorld extends ServerWorld {

    private final LevelProperties levelProperties;

    public FortressServerWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, LevelProperties properties, RegistryKey<World> worldKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime) {
        super(server, workerExecutor, session, properties, worldKey, dimensionType, worldGenerationProgressListener, chunkGenerator, debugWorld, seed, spawners, shouldTickTime);
        levelProperties = properties;
    }

    @Override
    public long getSeed() {
        return levelProperties.getGeneratorOptions().getSeed();
    }

    @Override
    public boolean isFlat() {
        return levelProperties.getGeneratorOptions().isFlatWorld();
    }
}
