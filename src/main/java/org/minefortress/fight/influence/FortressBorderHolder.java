package org.minefortress.fight.influence;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.FortressBorder;
import org.minefortress.utils.ModUtils;

import java.util.*;

public class FortressBorderHolder {

    private static final int FORTRESS_BORDER_SIZE = 64;
    private final ClientInfluenceManager clientInfluenceManager;
    private final List<BlockPos> allInfluencePositions = new ArrayList<>();

    private WorldBorder fortressBorder;

    public FortressBorderHolder(ClientInfluenceManager clientInfluenceManager) {
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
        final var positionsForBorder = new ArrayList<BlockPos>();
        final var fortressCenter = ModUtils.getFortressClientManager().getFortressCenter();
        if(fortressCenter == null) {
            fortressBorder = null;
            return;
        }
        positionsForBorder.add(fortressCenter);
        positionsForBorder.addAll(allInfluencePositions);
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
        final var x = center.getX();
        final var z = center.getZ();
        final var xSign = Math.signum(x);
        final var zSign = Math.signum(z);
        final var nonZeroSignX = xSign == 0 ? 1 : xSign;
        final var nonZeroSignZ = zSign == 0 ? 1 : zSign;
        final var adjustedX = x - x % FORTRESS_BORDER_SIZE + nonZeroSignX * FORTRESS_BORDER_SIZE / 2f;
        final var adjustedZ = z - z % FORTRESS_BORDER_SIZE + nonZeroSignZ * FORTRESS_BORDER_SIZE / 2f;
        border.setCenter(adjustedX, adjustedZ);
        border.setSize(FORTRESS_BORDER_SIZE);
    }

}
