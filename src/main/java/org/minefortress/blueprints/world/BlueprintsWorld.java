package org.minefortress.blueprints.world;


import com.mojang.serialization.Lifecycle;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintsWorld;
import net.remmintan.mods.minefortress.core.interfaces.server.IBlueprintEditingWorld;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import net.remmintan.mods.minefortress.core.utils.ModPathUtils;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

public class BlueprintsWorld implements IBlueprintsWorld {


    private static final GameRules EDIT_BLUEPRINT_RULES = Util.make(new GameRules(), gameRules -> {
        gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, null);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, null);
        gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
    });
    private static final LevelInfo EDIT_BLUEPRINT_LEVEL = new LevelInfo("Edit Blueprint Level",
            GameMode.CREATIVE,
            false,
            Difficulty.PEACEFUL,
            false,
            EDIT_BLUEPRINT_RULES,
            DataConfiguration.SAFE_MODE);
    private static final Identifier BLUEPRINTS_WORLD_ID = new Identifier("blueprints");
    public static final RegistryKey<World> BLUEPRINTS_WORLD_REGISTRY_KEY = RegistryKey.of(RegistryKeys.WORLD, BLUEPRINTS_WORLD_ID);
    private static final DimensionType BLUEPRINT_DIMENSION_TYPE = new DimensionType(
            OptionalLong.of(10000),
            true,
            false,
            false,
            false,
            1,
            false,
            true,
            0,
            32,
            16,
            BlockTags.INFINIBURN_OVERWORLD,
            BLUEPRINTS_WORLD_ID,
            0.0f,
            new DimensionType.MonsterSettings(false, false, UniformIntProvider.create(0, 7), 0)
    );

    private FortressServerWorld world;
    private final MinecraftServer server;
    private Map<BlockPos, BlockState> preparedBlueprintData;

    private LevelStorage.Session fortressSession = null;

    public BlueprintsWorld(MinecraftServer server) {
        this.server = server;
    }

    public IBlueprintEditingWorld getWorld() {
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

        this.fortressSession = ModPathUtils.getInstance().getBlueprintsWorldSession();

        final Registry<Biome> biomeRegistry = dynamicRegistryManager.get(RegistryKeys.BIOME);
        final Registry<DimensionType> dimensionTypeRegistry = dynamicRegistryManager.get(RegistryKeys.DIMENSION_TYPE);

        final ChunkGenerator chunkGenerator = new FlatChunkGenerator(getGeneratorConfig(biomeRegistry));


        final GeneratorOptions generatorOptions = new GeneratorOptions(0L, false, false);

        final LevelProperties levelProperties = new LevelProperties(EDIT_BLUEPRINT_LEVEL, generatorOptions, LevelProperties.SpecialProperty.FLAT,  Lifecycle.stable());
        world = new FortressServerWorld(
                server,
                executor,
                fortressSession,
                levelProperties,
                BLUEPRINTS_WORLD_REGISTRY_KEY,
                new DimensionOptions(dimensionTypeRegistry.getEntry(DimensionTypes.OVERWORLD).orElseThrow(), chunkGenerator),
                ((IFortressServer)server).get_WorldGenerationProgressListener()
        );
    }


    public static FlatChunkGeneratorConfig getGeneratorConfig(Registry<Biome> biomeRegistry) {

        final List<FlatChunkGeneratorLayer> flatChunkGeneratorLayers = Arrays.asList(
                new FlatChunkGeneratorLayer(1, Blocks.BEDROCK),
                new FlatChunkGeneratorLayer(14, Blocks.DIRT),
                new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK)
        );

        return new FlatChunkGeneratorConfig(Optional.empty(), biomeRegistry.getEntry(BiomeKeys.PLAINS).orElseThrow(), Collections.emptyList())
                .with(flatChunkGeneratorLayers, Optional.empty(), biomeRegistry.getEntry(BiomeKeys.PLAINS).orElseThrow());
    }

    public void prepareBlueprint(Map<BlockPos, BlockState> blueprintData, String blueprintFileName, int floorLevel, BlueprintGroup group) {
        this.preparedBlueprintData = blueprintData;
        final var world = getWorld();
        world.setFileName(blueprintFileName);
        world.setFloorLevel(floorLevel);
        world.setBlueprintGroup(group);
    }

    @Override
    public void clearBlueprint(ServerPlayerEntity player) {
        preparedBlueprintData = new HashMap<>();
        putBlueprintInAWorld(player, new Vec3i(1, 1, 1));
    }

    public void putBlueprintInAWorld(final ServerPlayerEntity player, Vec3i blueprintSize) {
        final BlockState borderBlockState = Blocks.RED_WOOL.getDefaultState();

        final var xOffset = (16 - blueprintSize.getX()) / 2;
        final var zOffset = (16 - blueprintSize.getZ()) / 2;

        final int defaultFloorLevel = 16;
        BlockPos
                .iterate(new BlockPos(-32, 0, -32), new BlockPos(32, 32, 32))
                .forEach(pos -> {
                    BlockState blockState;
                    final var offsetPos = pos
                            .down(defaultFloorLevel - getWorld().getFloorLevel())
                            .add(-xOffset, 0, -zOffset);

                    if(preparedBlueprintData.containsKey(offsetPos)) {
                        blockState = preparedBlueprintData.get(offsetPos);
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

        BlockPos.iterate(new BlockPos(-1, 15, -1), new BlockPos(16, 15, 16)).forEach(pos -> {
            if(pos.getZ() == -1 || pos.getZ() == 16 || pos.getX() == -1 || pos.getX() == 16) {
                world.setBlockState(pos, borderBlockState);
                world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
            }
        });

        BlockPos.iterate(new BlockPos(16, 15, 16), new BlockPos(16, 31, 16)).forEach(pos -> {
            world.setBlockState(pos, borderBlockState);
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

    public static boolean isBlueprintsWorld(@Nullable World world) {
        return world != null && world.getRegistryKey().equals(BLUEPRINTS_WORLD_REGISTRY_KEY);
    }

}
