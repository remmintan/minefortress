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
import org.minefortress.renderer.gui.blueprints.BlueprintGroup;

import java.util.Collections;
import java.util.concurrent.Executor;

public class FortressServerWorld extends ServerWorld {

    private final LevelProperties levelProperties;
    private WorldBorder blueprintsWorldBorder;

    private boolean saveModeEnabled = false;

    private String fileName;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public void setFloorLevel(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    public BlueprintGroup getBlueprintGroup() {
        return blueprintGroup;
    }

    public void setBlueprintGroup(BlueprintGroup blueprintGroup) {
        this.blueprintGroup = blueprintGroup;
    }

    public void enableSaveStructureMode() {
        saveModeEnabled = true;
    }

    public void disableSaveStructureMode() {
        saveModeEnabled = false;
    }
}
