package org.minefortress.entity.ai.goal.hostile;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.blocks.FortressBlocks;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IServerBuildingsManager;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;

import java.util.EnumSet;


public final class AttackBuildingGoal extends Goal {

    private static final int ATTACK_DISTANCE = 6;
    private final HostileEntity mob;
    private IFortressBuilding targetBuilding;
    private BlockPos targetPosition;
    private Path path;
    private long lastUpdateTime = 0;

    private int cooldown = 0;

    public AttackBuildingGoal(HostileEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if(lastUpdateTime + 20 > mob.getWorld().getTime()) return false;
        if(!mob.getWorld().isNight()) return false;
        lastUpdateTime = mob.getWorld().getTime();

        final var mobBlockPos = this.mob.getBlockPos();
        final var followRange = (int) getFollowRange();

        IServerBuildingsManager buildingsManager = null;
        for (var pos : BlockPos.iterateOutwards(mobBlockPos, followRange, 10, followRange)) {
            if (this.mob.getWorld().getBlockState(pos).isOf(FortressBlocks.FORTRESS_CAMPFIRE)) {
                buildingsManager = ServerModUtils.getManagersProvider(mob.getServer(), pos).getBuildingsManager();
                break;
            }
        }

        if (buildingsManager != null) {
            buildingsManager
                    .findNearest(mobBlockPos)
                    .ifPresent(building -> this.targetBuilding = building);
        }

        if(targetBuilding!=null) {
            this.targetPosition = targetBuilding.getNearestCornerXZ(mobBlockPos, mob.getWorld());
            this.path = mob.getNavigation().findPathTo(targetPosition, ATTACK_DISTANCE);
        }

        if(path != null) {
            return true;
        } else {
            stop();
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingAlong(this.path, 1.2);
    }

    @Override
    public void tick() {
        final var navigation = mob.getNavigation();
        if(!navigation.isFollowingPath() && didntReachTheTarget()) {
            path = navigation.findPathTo(targetPosition, ATTACK_DISTANCE);
            if(path != null)
                navigation.startMovingAlong(path, 1.2);
        }

        attack();
        cooldown--;
    }

    @Override
    public boolean shouldContinue() {
        return mob.getWorld().isNight()
                && path != null
                && targetBuilding.getHealth() > 0
                && (mob.getAttacker() == null || !mob.getAttacker().isAlive());
    }

    @Override
    public boolean canStop() {
        return false;
    }

    @Override
    public void stop() {
        this.targetBuilding = null;
        this.targetPosition = null;
        this.path = null;
    }

    private void attack() {
        if(targetBuilding == null) return;
        if(targetPosition == null) return;
        if(didntReachTheTarget()) return;
        this.mob.getLookControl().lookAt(targetPosition.getX(), targetPosition.getY(), targetPosition.getZ(), 30, 30);
        if(cooldown <= 0) {
            this.mob.swingHand(Hand.MAIN_HAND);
            this.targetBuilding.attack(this.mob);
            cooldown = this.getTickCount(20);
        }
    }

    private boolean didntReachTheTarget() {
        return targetPosition.getSquaredDistance(mob.getPos()) > ATTACK_DISTANCE * ATTACK_DISTANCE;
    }

    private double getFollowRange() {
        return this.mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
    }
}
