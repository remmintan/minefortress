package org.minefortress.blueprints.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup;
import net.remmintan.mods.minefortress.core.interfaces.server.IBlueprintEditingWorld;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.concurrent.Executor;

public class FortressServerWorld extends ServerWorld implements IBlueprintEditingWorld {

    private final LevelProperties levelProperties;
    private WorldBorder blueprintsWorldBorder;

    private boolean saveModeEnabled = false;

    private String blueprintId = "";
    private String blueprintName;
    private int floorLevel;
    private BlueprintGroup blueprintGroup;

    public FortressServerWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, LevelProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, false, 0L, Collections.emptyList(), false, new RandomSequencesState(0));

        levelProperties = properties;
        blueprintsWorldBorder.setCenter(8, 8);
        blueprintsWorldBorder.setSize(16);
    }

    @Override
    public long getSeed() {
        return levelProperties != null ? levelProperties.getGeneratorOptions().getSeed() : 0;
    }

    @Override
    public boolean isFlat() {
        return true;
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

    @Override
    public BlockState getBlockState(BlockPos pos) {
        final BlockState blockState = super.getBlockState(pos);
        if(saveModeEnabled) {
            if(pos.getY() > 0 && pos.getY() <= 15 && (blockState == Blocks.GRASS_BLOCK.getDefaultState() || blockState == Blocks.DIRT.getDefaultState())) return Blocks.VOID_AIR.getDefaultState();
        }
        return blockState;
    }

    @Override
    public int getFloorLevel() {
        return floorLevel;
    }

    @Override
    public void setFloorLevel(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    @Nullable
    @Override
    public String getBlueprintId() {
        return this.blueprintId;
    }

    @Override
    public void setBlueprintId(@Nullable String s) {
        this.blueprintId = s;
    }

    @Nullable
    @Override
    public String getBlueprintName() {
        return this.blueprintName;
    }

    @Override
    public void setBlueprintName(@Nullable String s) {
        this.blueprintName = s;
    }

    @Override
    public void enableSaveStructureMode() {
        saveModeEnabled = true;
    }

    @Override
    public void disableSaveStructureMode() {
        saveModeEnabled = false;
    }

    @Nullable
    @Override
    public BlueprintGroup getBlueprintGroup() {
        return blueprintGroup;
    }

    @Override
    public void setBlueprintGroup(@Nullable BlueprintGroup group) {
        this.blueprintGroup = group;
    }
}
