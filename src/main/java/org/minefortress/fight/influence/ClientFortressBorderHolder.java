package org.minefortress.fight.influence;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.FortressBorder;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.utils.ModUtils;
import Z;
import java.util.*;

public class ClientFortressBorderHolder extends BaseFortressBorderHolder {
    private final ClientInfluenceManager clientInfluenceManager;
    private final List<BlockPos> allInfluencePositions = new ArrayList<>();

    private WorldBorder fortressBorder;

    public ClientFortressBorderHolder(ClientInfluenceManager clientInfluenceManager) {
        this.clientInfluenceManager = clientInfluenceManager;
    }

    public void syncInfluencePositions(List<BlockPos> positions) {
        allInfluencePositions.clear();
        allInfluencePositions.addAll(positions);
        rebuildFortressBorder();
    }

    public Optional<WorldBorder> getFortressBorder() {
        final var fortressClientManager = ModUtils.getFortressClientManager();
        if(fortressClientManager.isCenterNotSet()) {
            final var posAppropriateForCenter = fortressClientManager.getPosAppropriateForCenter();
            if(posAppropriateForCenter == null) {
                return Optional.empty();
            }
            return getWorldBorder(Collections.singletonList(posAppropriateForCenter), true);
        }
        final var selecting = clientInfluenceManager.isSelecting();
        if(selecting) {
            return clientInfluenceManager
                    .getStructureRenderPos()
                    .flatMap(it -> getWorldBorder(Collections.singletonList(it), true));
        }
        return Optional.ofNullable(fortressBorder);
    }

    private void rebuildFortressBorder() {
        final var positionsForBorder = new ArrayList<>(allInfluencePositions);
        fortressBorder = getWorldBorder(positionsForBorder).orElse(null);
    }

    @NotNull
    private static Optional<WorldBorder> getWorldBorder(List<BlockPos> pos) {
        return getWorldBorder(pos, false);
    }

    @NotNull
    private static Optional<WorldBorder> getWorldBorder(List<BlockPos> positions, boolean dynamicState) {
        if(positions != null && !positions.isEmpty()) {
            final var posQueue = new ArrayDeque<>(positions);

            BlockPos mainPos = posQueue.poll();
            if(mainPos == null) {
                return Optional.empty();
            }
            final var border = new FortressBorder();
            createBorder(border, mainPos);
            if (dynamicState) {
                border.enableDynamicStage();
            }
            posQueue.forEach(pos -> border.addAdditionalBorder(createBorder(pos)));

            return Optional.of(border);
        } else {
            return Optional.empty();
        }
    }

    private static WorldBorder createBorder(BlockPos center) {
        final var border = new WorldBorder();
        createBorder(border, center);
        return border;
    }

    private static void createBorder(WorldBorder border, BlockPos center) {
        final BlockPos adjustedPos = alignToAGrid(center);
        border.setCenter(adjustedPos.getX(), adjustedPos.getZ());
        border.setSize(FORTRESS_BORDER_SIZE);
    }
}
