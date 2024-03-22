package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.remmintan.gobi.helpers.TreeBlocks;
import net.remmintan.gobi.helpers.TreeHelper;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITask;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressServerNetworkHelper;
import net.remmintan.mods.minefortress.networking.s2c.ClientboundTaskExecutedPacket;
import org.minefortress.tasks.block.info.DigTaskBlockInfo;

import java.util.*;

public class CutTreesTask implements ITask {

    private final UUID uuid;
    private final Queue<BlockPos> treeRoots;
    private final int totalRootCount;

    private int removedRoots = 0;
    private boolean canceled = false;
    private final List<BlockPos> positions;

    public CutTreesTask(UUID uuid, List<BlockPos> treeRoots, List<BlockPos> positions) {
        this.uuid = uuid;
        this.treeRoots = new ArrayDeque<>(treeRoots);
        this.totalRootCount = treeRoots.size();
        this.positions = positions;
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.REMOVE;
    }

    @Override
    public boolean hasAvailableParts() {
        return !treeRoots.isEmpty();
    }

    @Override
    public ITaskPart getNextPart(IWorkerPawn colonist) {
        if(!treeRoots.isEmpty()) {
            final BlockPos root = treeRoots.remove();
            final ITaskBlockInfo rootBlockInfo = new DigTaskBlockInfo( root);
            return new TaskPart(Pair.of(root, root), Collections.singletonList(rootBlockInfo), this);
        } else {
            return null;
        }
    }

    @Override
    public void returnPart(Pair<BlockPos, BlockPos> partStartAndEnd) {
        final BlockPos root = partStartAndEnd.getFirst();
        treeRoots.add(root);
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void finishPart(ITaskPart part, IWorkerPawn colonist) {
        final ServerWorld world = colonist.getServerWorld();
        final BlockPos root = part.getStartAndEnd().getFirst();
        final Optional<TreeBlocks> treeOpt = TreeHelper.getTreeBlocks(root.up(), world);
        if(treeOpt.isPresent()) {
            final TreeBlocks tree = treeOpt.get();
            TreeHelper.removeTheRestOfATree(colonist, tree, world);
        }

        removedRoots++;
        if(removedRoots > totalRootCount) {
            throw new IllegalStateException("Removed more roots than total roots");
        }

        if(treeRoots.isEmpty() && removedRoots == totalRootCount) {
            world.getPlayers().stream().findAny().ifPresent(player -> FortressServerNetworkHelper.send(player, FortressChannelNames.FINISH_TASK, new ClientboundTaskExecutedPacket(this.getId())));
        }
    }

    @Override
    public boolean taskFullyFinished() {
        return removedRoots == totalRootCount;
    }

    @Override
    public List<TaskInformationDto> toTaskInformationDto() {
        final var taskInformationDto = new TaskInformationDto(getId(), positions, TaskType.REMOVE);
        return List.of(taskInformationDto);
    }
}
