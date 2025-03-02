package org.minefortress.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
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
        final var managersProvider = ServerModUtils.getManagersProvider(pawn);
        final var buildingsManager = managersProvider.getBuildingsManager();
        buildingsManager.getRandomBuildingAttacker()
                .ifPresent(it -> this.targetEntity = it);
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
