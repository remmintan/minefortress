package org.minefortress.tasks;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.selections.SelectionType;

import java.util.UUID;

public class BlueprintDigTask extends SimpleSelectionTask {
    public BlueprintDigTask(UUID id, BlockPos startingBlock, BlockPos endingBlock) {
        super(id, TaskType.REMOVE, startingBlock, endingBlock, null, SelectionType.SQUARES);
    }

    @Override
    protected void sendFinishTaskNotificationToPlayer(ServerPlayerEntity randomPlayer) {}
}
