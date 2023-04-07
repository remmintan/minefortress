package org.minefortress.fight.influence;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import org.minefortress.blueprints.interfaces.IBlockDataProvider;
import org.minefortress.blueprints.manager.BaseClientStructureManager;
import org.minefortress.blueprints.manager.BlueprintMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientInfluenceManager extends BaseClientStructureManager {

    private static final BlueprintMetadata INFLUENCE_FLAG_METADATA = new BlueprintMetadata("Influence Flag", "influence_flag", 0, null);

    private final InfluenceFlagBlockDataProvider blockDataProvider = new InfluenceFlagBlockDataProvider();

    private final List<BlockPos> allInfluencePositions = new ArrayList<>();
    private boolean isSelectingInfluencePosition = false;

    public ClientInfluenceManager(MinecraftClient client) {
        super(client);
    }

    public void addInfluencePosition(BlockPos pos) {
        allInfluencePositions.add(pos);
    }

    public void removeInfluencePosition(BlockPos pos) {
        allInfluencePositions.remove(pos);
    }

    public List<BlockPos> getAllInfluencePositions() {
        return Collections.unmodifiableList(allInfluencePositions);
    }

    public void startSelectingInfluencePosition() {
        isSelectingInfluencePosition = true;
    }

    public void cancelSelectingInfluencePosition() {
        super.reset();
        isSelectingInfluencePosition = false;
    }

    public void selectInfluencePosition() {
        isSelectingInfluencePosition = false;
        final var pos = getStructureBuildPos();
        if(pos != null) {
            addInfluencePosition(pos);
            super.reset();
        }
    }

    @Override
    public IBlockDataProvider getBlockDataProvider() {
        return blockDataProvider;
    }

    @Override
    public boolean isSelecting() {
        return isSelectingInfluencePosition;
    }

    @Override
    public BlueprintMetadata getSelectedStructure() {
        return INFLUENCE_FLAG_METADATA;
    }
}
