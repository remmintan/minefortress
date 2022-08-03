package org.minefortress.entity.ai.goal;

import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressBedInfo;
import org.minefortress.utils.BuildingHelper;

public class HideGoal extends AbstractFortressGoal{

    private int hideTicks = 0;
    private BlockPos shelterBlockPos;
    private BlockPos moveGoal;

    public HideGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        return isScared() || isHiding();
    }

    @Override
    public void start() {
        final var fortressServerManager = colonist
                .getFortressServerManager();
        fortressServerManager
                .getServerFightManager()
                .addScaryMob(colonist.getTarget());
        shelterBlockPos = fortressServerManager
                .getRandomBed()
                .map(FortressBedInfo::getPos)
                .orElse(fortressServerManager.getFortressCenter());

        if(shelterBlockPos == null) return;
        this.moveGoal = findCorrectGoal();
        colonist.getMovementHelper().set(moveGoal);
        this.hideTicks = 100;
    }

    @Override
    public void tick() {
        final var movementHelper = colonist.getMovementHelper();
        if(movementHelper.hasReachedWorkGoal()) {
            this.hideTicks--;
        }
    }

    @Override
    public boolean shouldContinue() {
        return shelterBlockPos!=null && (this.hideTicks > 0 || super.isHiding()) && !this.colonist.getMovementHelper().isStuck();
    }

    @Override
    public void stop() {
        super.stop();
        this.hideTicks = 0;
        this.moveGoal = null;
        this.shelterBlockPos = null;
        colonist.getMovementHelper().reset();
    }

    private BlockPos findCorrectGoal() {
        if(BuildingHelper.canStayOnBlock(colonist.world, shelterBlockPos)) return shelterBlockPos;
        for(BlockPos pos : BlockPos.iterateOutwards(shelterBlockPos, 3, 3, 3)) {
            if(BuildingHelper.canStayOnBlock(colonist.world, pos)) return pos;
        }

        return shelterBlockPos;
    }

}
