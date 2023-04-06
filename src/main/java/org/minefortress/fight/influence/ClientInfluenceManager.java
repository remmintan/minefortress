package org.minefortress.fight.influence;

import net.minecraft.util.math.BlockPos;
import org.minefortress.blueprints.interfaces.IBlockDataProvider;
import org.minefortress.blueprints.interfaces.IStructureRenderInfoProvider;
import org.minefortress.blueprints.manager.BlueprintMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ClientInfluenceManager implements IStructureRenderInfoProvider {

    private static final BlueprintMetadata INFLUENCE_FLAG_METADATA = new BlueprintMetadata("Influence Flag", "influence_flag", 0, null);

    private final InfluenceFlagBlockDataProvider blockDataProvider = new InfluenceFlagBlockDataProvider();

    private final List<BlockPos> allInfluencePositions = new ArrayList<>();
    private BlockPos currentPossibleInfluencePosition = null;
    private boolean isSelectingInfluencePosition = false;

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
        isSelectingInfluencePosition = false;
        currentPossibleInfluencePosition = null;
    }

    public void selectInfluencePosition() {
        isSelectingInfluencePosition = false;
        if(currentPossibleInfluencePosition != null) {
            addInfluencePosition(currentPossibleInfluencePosition);
            currentPossibleInfluencePosition = null;
        }
    }

    public IBlockDataProvider getBlockDataProvider() {
        return blockDataProvider;
    }

    @Override
    public boolean isSelecting() {
        return isSelectingInfluencePosition;
    }

    @Override
    public Optional<BlockPos> getStructureRenderPos() {
        return Optional.empty();
    }

    @Override
    public boolean canBuild() {
        return false;
    }

    @Override
    public BlueprintMetadata getSelectedStructure() {
        return INFLUENCE_FLAG_METADATA;
    }
}
