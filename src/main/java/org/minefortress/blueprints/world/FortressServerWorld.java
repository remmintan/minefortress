package org.minefortress.blueprints.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.minefortress.interfaces.FortressServer;

import java.util.List;
import java.util.concurrent.Executor;

public class FortressServerWorld extends ServerWorld {

    private final LevelProperties levelProperties;
    private WorldBorder blueprintsWorldBorder;

    public FortressServerWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, LevelProperties properties, RegistryKey<World> worldKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime) {
        super(server, workerExecutor, session, properties, worldKey, dimensionType, worldGenerationProgressListener, chunkGenerator, debugWorld, seed, spawners, shouldTickTime);
        levelProperties = properties;
        blueprintsWorldBorder.setCenter(8, 8);
        blueprintsWorldBorder.setSize(16);
    }

    @Override
    public long getSeed() {
        return levelProperties.getGeneratorOptions().getSeed();
    }

    @Override
    public boolean isFlat() {
        return levelProperties.getGeneratorOptions().isFlatWorld();
    }

    @Override
    public boolean isSavingDisabled() {
        return true;
    }

    @Override
    public WorldBorder getWorldBorder() {
        if(blueprintsWorldBorder == null) {
            blueprintsWorldBorder = new WorldBorder();
        }
        return blueprintsWorldBorder;
    }
}
