package org.minefortress.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.interfaces.server.IServerManagersProvider;
import org.minefortress.entity.Colonist;

public class BuildingAttackerTargetGoal extends TrackTargetGoal {

    private final Colonist pawn;
    protected LivingEntity targetEntity;

    public BuildingAttackerTargetGoal(Colonist pawn) {
        super(pawn, false);
        this.pawn = pawn;
    }

    @Override
    public boolean canStart() {
        if(this.pawn.getRandom().nextInt(10) != 0) {
            return false;
        }

        findRandomBuildingAttacker();

        return this.targetEntity != null;
    }

    private void findRandomBuildingAttacker() {
        pawn
            .getManagersProvider()
            .map(IServerManagersProvider::getBuildingsManager)
            .flatMap(IServerBuildingsManager::getRandomBuildingAttacker)
            .ifPresent(target -> this.targetEntity = target);
    }

    @Override
    public void start() {
        this.pawn.setTarget(this.targetEntity);
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.targetEntity = null;
    }
}
