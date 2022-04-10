package org.minefortress.blueprints.world;

import com.mojang.serialization.Lifecycle;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.LogManager;
import org.minefortress.interfaces.FortressServer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

public class BlueprintsWorld {

    private static final String MOD_DIR = "minefortress";
    private static final String WORLD_DIR_PREFIX = "blueprints";
    private static final GameRules EDIT_BLUEPRINT_RULES = Util.make(new GameRules(), gameRules -> {
        gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, null);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, null);
        gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
    });
    private static final LevelInfo EDIT_BLUEPRINT_LEVEL = new LevelInfo("Edit Blueprint Level", GameMode.CREATIVE, false, Difficulty.PEACEFUL, false, EDIT_BLUEPRINT_RULES, DataPackSettings.SAFE_MODE);
    private static final Identifier BLUEPRINTS_WORLD_ID = new Identifier("blueprints");
    public static final RegistryKey<World> BLUEPRINTS_WORLD_REGISTRY_KEY = RegistryKey.of(Registry.WORLD_KEY, BLUEPRINTS_WORLD_ID);
    private static final DimensionType BLUEPRINT_DIMENSION_TYPE = DimensionType.create(
            OptionalLong.of(10000),
            true,
            false,
            false,
            false,
            1,
            false,
            true,
            true,
            false,
            false,
            0,
            32,
            16,
            BlockTags.INFINIBURN_OVERWORLD.getId(),
            BLUEPRINTS_WORLD_ID,
            0.0f
    );

    private FortressServerWorld world;
    private final MinecraftServer server;
    private Map<BlockPos, BlockState> preparedBlueprintData;

    private LevelStorage.Session fortressSession = null;

    public BlueprintsWorld(MinecraftServer server) {
        this.server = server;
    }

    public FortressServerWorld getWorld() {
        if(world == null) {
            create();
        }

        return world;
    }

    public void tick(BooleanSupplier shouldKeepTicking) {
        if(world != null) {
            world.tick(shouldKeepTicking);
        }
    }

    public void sendToDimension(PlayerManager playerManager) {
        if(world != null) {
            playerManager.sendToDimension(new WorldTimeUpdateS2CPacket(world.getTime(), world.getTimeOfDay(), world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)), world.getRegistryKey());
        }
    }

    private void create() {
        final Executor executor = Util.getMainWorkerExecutor();
        final DynamicRegistryManager dynamicRegistryManager = server.getRegistryManager();
        final File runDirectory = MinecraftClient.getInstance().runDirectory;

        final LevelStorage fortressLevelStorage = LevelStorage.create(runDirectory.toPath().resolve(MOD_DIR));
        try {
            fortressSession = fortressLevelStorage.createSession(WORLD_DIR_PREFIX+UUID.randomUUID());
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Registry<Biome> biomeRegistry = dynamicRegistryManager.get(Registry.BIOME_KEY);
        final Registry<DimensionType> dimensionTypeRegistry = dynamicRegistryManager.get(Registry.DIMENSION_TYPE_KEY);
        final Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry = dynamicRegistryManager.get(Registry.CHUNK_GENERATOR_SETTINGS_KEY);

        final SimpleRegistry<DimensionOptions> dimensionOptions = DimensionType.createDefaultDimensionOptions(
                dynamicRegistryManager,
                0L
        );

        final ChunkGenerator chunkGenerator = new FlatChunkGenerator(getGeneratorConfig(biomeRegistry));

        final SimpleRegistry<DimensionOptions> updatedDimensionOptions = GeneratorOptions.getRegistryWithReplacedOverworldGenerator(
                dimensionTypeRegistry,
                dimensionOptions,
                chunkGenerator
        );

        final GeneratorOptions generatorOptions = new GeneratorOptions(0L, false, false, updatedDimensionOptions);

        final LevelProperties levelProperties = new LevelProperties(EDIT_BLUEPRINT_LEVEL, generatorOptions, Lifecycle.stable());

        world = new FortressServerWorld(
                server,
                executor,
                fortressSession,
                levelProperties,
                BLUEPRINTS_WORLD_REGISTRY_KEY,
                BLUEPRINT_DIMENSION_TYPE,
                ((FortressServer)server).getWorldGenerationProgressListener(),
                chunkGenerator,
                false,
                0L,
                Collections.emptyList(),
                false
        );
    }


    public static FlatChunkGeneratorConfig getGeneratorConfig(Registry<Biome> biomeRegistry) {
        final StructuresConfig structuresConfig = new StructuresConfig(Optional.empty(), Collections.emptyMap());

        final List<FlatChunkGeneratorLayer> flatChunkGeneratorLayers = Arrays.asList(
                new FlatChunkGeneratorLayer(1, Blocks.BEDROCK),
                new FlatChunkGeneratorLayer(14, Blocks.DIRT),
                new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK)
        );

        return FlatChunkGeneratorConfig
                .getDefaultConfig(biomeRegistry)
                .withLayers(flatChunkGeneratorLayers, structuresConfig);
    }

    public void prepareBlueprint(Map<BlockPos, BlockState> blueprintData, String blueprintFileName, int floorLevel) {
        this.preparedBlueprintData = blueprintData;
        final FortressServerWorld world = getWorld();
        world.setFileName(blueprintFileName);
        world.setFloorLevel(floorLevel);
    }

    public void putBlueprintInAWorld(final ServerPlayerEntity player) {
        final BlockState borderBlockState = Blocks.RED_WOOL.getDefaultState();
        BlockPos.iterate(new BlockPos(-1, 15, -1), new BlockPos(16, 15, 16)).forEach(pos -> {
            if(pos.getZ() == -1 || pos.getZ() == 16 || pos.getX() == -1 || pos.getX() == 16) {
                world.setBlockState(pos, borderBlockState);
                world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
            }
        });

        final int defaultFloorLevel = 16;
        BlockPos
                .iterate(new BlockPos(BlockPos.ZERO), new BlockPos(15, 32, 15))
                .forEach(pos -> {
                    BlockState blockState;
                    if(preparedBlueprintData.containsKey(pos.down(defaultFloorLevel - getWorld().getFloorLevel()))) {
                        blockState = preparedBlueprintData.get(pos.down(defaultFloorLevel - getWorld().getFloorLevel()));
                    } else if(pos.getY() >= defaultFloorLevel) {
                        blockState = Blocks.AIR.getDefaultState();
                    } else if(pos.getY() == 0) {
                        blockState = Blocks.BEDROCK.getDefaultState();
                    } else if(pos.getY() > 0 && pos.getY() < defaultFloorLevel - 2) {
                        blockState = Blocks.DIRT.getDefaultState();
                    } else {
                        blockState = Blocks.GRASS_BLOCK.getDefaultState();
                    }

                    world.setBlockState(pos, blockState);
                    world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
                });
    }

    public void closeSession() {
        if(fortressSession != null) {
            try {
                fortressSession.close();
            }catch (IOException e) {
                LogManager.getLogger().error("Failed to unlock level {}", this.fortressSession.getDirectoryName(), e);
            }
        }
    }

    public boolean hasWorld() {
        return world != null;
    }

}
