package org.minefortress.entity.ai.goal.warrior;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.BlockRotation;
import org.jetbrains.annotations.Nullable;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintDataLayer;
import org.minefortress.entity.WarriorPawn;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IBaritoneMoveControl;
import org.minefortress.fight.influence.ServerInfluenceManager;
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket;

import java.util.EnumSet;

public class CapturePositionGoal extends Goal {

    private final WarriorPawn pawn;
    @Nullable
    private ServerInfluenceManager.CaptureTask target;

    public CapturePositionGoal(WarriorPawn pawn) {
        this.pawn = pawn;
        this.setControls(EnumSet.of(Control.MOVE, Control.JUMP));
    }
    @Override
    public boolean canStart() {
        final var fsmOpt = pawn.getFortressServerManager();
        if(fsmOpt.isEmpty())
            return false;
        final var fsm = fsmOpt.get();
        final var influenceManager = fsm.getInfluenceManager();
        target = influenceManager.getCaptureTask();
        return target != null;
    }

    @Override
    public void start() {
        if(target == null) return;
        pawn.resetTargets();
        final var moveControl = pawn.getFortressMoveControl();
        moveControl.moveTo(target.pos());
    }

    @Override
    public void tick() {
        if(target == null) return;
        final var moveControl = pawn.getFortressMoveControl();
        if(moveControl.isStuck()) {
            teleportToGoal(moveControl);
        }
        if(this.hasReachedTarget()) {
            finish();
        }
    }

    private void teleportToGoal(IBaritoneMoveControl moveControl) {
        if(target == null) return;
        moveControl.reset();
        final var pos = target.pos();
        pawn.teleport(pos.getX(), pos.getY()+1, pos.getZ());
    }

    private void finish() {
        if(target == null) return;
        pawn.getFortressServerManager()
                .ifPresent(it -> {
                    final var resourceManager = (IServerResourceManager)it.getResourceManager();
                    final var influenceManager = it.getInfluenceManager();
                    final var flagDataProvider = influenceManager.getBlockDataProvider();
                    final var influenceFlag = flagDataProvider.getBlockData("influence_flag", BlockRotation.NONE);
                    final var targetPos = target.pos();
                    influenceFlag.getLayer(BlueprintDataLayer.GENERAL)
                            .forEach((pos, state) -> {
                                final var realpos = pos.add(targetPos);
                                pawn.getWorld().setBlockState(realpos, state, 3);
                                if(it.isSurvival()) {
                                    resourceManager.removeReservedItem(target.taskId(), state.getBlock().asItem());
                                }
                            });
                    influenceManager.addInfluencePosition(target.pos());
                });

        pawn.getMasterPlayer()
                .ifPresent(it -> {
                    final var packet = new ClientboundTaskExecutedPacket(target.taskId());
                    FortressServerNetworkHelper.send(it, FortressChannelNames.FINISH_TASK, packet);
                });

        target = null;
    }

    @Override
    public boolean shouldContinue() {
        return  target != null;
    }

    @Override
    public boolean canStop() {
        return true;
    }

    @Override
    public void stop() {
        if(target != null) {
            pawn.getFortressServerManager()
                    .ifPresent(it -> it.getInfluenceManager().failCaptureTask(target));
            target = null;
        }
        pawn.getFortressMoveControl().reset();
    }

    private boolean hasReachedTarget() {
        return target != null && target.pos().isWithinDistance(pawn.getPos(), pawn.getTargetMoveRange() + 1);
    }
}
