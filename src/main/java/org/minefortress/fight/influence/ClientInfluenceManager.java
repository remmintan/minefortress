package org.minefortress.fight.influence;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.minefortress.blueprints.interfaces.IBlockDataProvider;
import org.minefortress.blueprints.manager.BaseClientStructureManager;
import org.minefortress.blueprints.manager.BlueprintMetadata;

import java.util.List;
import java.util.Optional;

public class ClientInfluenceManager extends BaseClientStructureManager {

    private static final BlueprintMetadata INFLUENCE_FLAG_METADATA = new BlueprintMetadata("Influence Flag", "influence_flag", 0, null);

    private final InfluenceFlagBlockDataProvider blockDataProvider = new InfluenceFlagBlockDataProvider();
    private final FortressBorderHolder fortressBorderHolder = new FortressBorderHolder(this);

    private boolean isSelectingInfluencePosition = false;

    public ClientInfluenceManager(MinecraftClient client) {
        super(client);
    }

    public Optional<WorldBorder> getFortressBorder() {
        return fortressBorderHolder.getFortressBorder();
    }

    public void startSelectingInfluencePosition() {
        isSelectingInfluencePosition = true;
    }

    public void cancelSelectingInfluencePosition() {
        super.reset();
        isSelectingInfluencePosition = false;
    }

    public void sync(List<BlockPos> positions) {
        fortressBorderHolder.syncInfluencePositions(positions);
    }

    public void selectInfluencePosition() {
        isSelectingInfluencePosition = false;
        final var pos = getStructureBuildPos();
        if(pos != null) {
            // TODO send packet to server
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
