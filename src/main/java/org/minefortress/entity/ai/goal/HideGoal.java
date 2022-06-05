package org.minefortress.entity.ai.goal;

import net.minecraft.util.math.BlockPos;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressBedInfo;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.tasks.BuildingManager;

public class HideGoal extends AbstractFortressGoal{

    private int hideTicks = 0;
    private BlockPos shelterBlockPos;
    private BlockPos moveGoal;

    public HideGoal(Colonist colonist) {
        super(colonist);
    }

    @Override
    public boolean canStart() {
        return isScared();
    }

    @Override
    public void start() {
        final var fortressServerManager = colonist.getFortressServerManager();
        shelterBlockPos = fortressServerManager
                .flatMap(it -> it.getRandomBed(colonist.world.random))
                .map(FortressBedInfo::getPos)
                .orElse(fortressServerManager.map(FortressServerManager::getFortressCenter).orElseThrow());

        this.moveGoal = findCorrectGoal();
        colonist.getMovementHelper().set(moveGoal);
        this.hideTicks = 100;
    }

    @Override
    public void tick() {
        final var movementHelper = colonist.getMovementHelper();
        movementHelper.tick();

        if(movementHelper.hasReachedWorkGoal()) {
            this.hideTicks--;
        }
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && this.hideTicks > 0 && !this.colonist.getMovementHelper().isCantFindPath();
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
        if(BuildingManager.canStayOnBlock(colonist.world, shelterBlockPos)) return shelterBlockPos;
        for(BlockPos pos : BlockPos.iterateOutwards(shelterBlockPos, 3, 3, 3)) {
            if(BuildingManager.canStayOnBlock(colonist.world, pos)) return pos;
        }

        return shelterBlockPos;
    }

}
