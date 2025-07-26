package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import net.remmintan.mods.minefortress.core.interfaces.selections.ServerSelectionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServerboundSimpleSelectionTaskPacket implements FortressC2SPacket {

    private final TaskType taskType;
    private final BlockPos start;
    private final BlockPos end;
    private final HitResult hitResult;
    private final String selectionType;
    private final List<Integer> selectedPawns;
    private final List<BlockPos> positions;

    public ServerboundSimpleSelectionTaskPacket(TaskType taskType, BlockPos start, BlockPos end, HitResult hitResult, String selectionType, List<BlockPos> positions, List<Integer> selectedPawns) {
        this.taskType = taskType;
        this.start = start;
        this.end = end;
        this.hitResult = hitResult;
        this.selectionType = selectionType;
        this.positions = positions;
        this.selectedPawns = selectedPawns;
    }

    public ServerboundSimpleSelectionTaskPacket(PacketByteBuf buffer) {
        this.taskType = buffer.readEnumConstant(TaskType.class);
        this.start = buffer.readBlockPos();
        this.end = buffer.readBlockPos();
        HitResult.Type hitType = buffer.readEnumConstant(HitResult.Type.class);
        if(hitType == HitResult.Type.BLOCK) {
            this.hitResult = buffer.readBlockHitResult();
        } else {
            this.hitResult = null;
        }
        this.selectionType = buffer.readString();

        this.positions = new ArrayList<>();
        int selectionSize = buffer.readInt();
        for (int i = 0; i < selectionSize; i++) {
            this.positions.add(buffer.readBlockPos());
        }

        this.selectedPawns = new ArrayList<>();
        int pawnsSize = buffer.readInt();
        for (int i = 0; i < pawnsSize; i++) {
            this.selectedPawns.add(buffer.readInt());
        }
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeEnumConstant(this.taskType);
        buffer.writeBlockPos(this.start);
        buffer.writeBlockPos(this.end);
        HitResult.Type type = Optional.ofNullable(this.hitResult).map(HitResult::getType).orElse(HitResult.Type.MISS);
        buffer.writeEnumConstant(type);
        if(type == HitResult.Type.BLOCK) {
            buffer.writeBlockHitResult((BlockHitResult) this.hitResult);
        }
        buffer.writeString(selectionType);

        buffer.writeInt(positions.size());
        for (BlockPos blockPos : positions) {
            buffer.writeBlockPos(blockPos);
        }

        buffer.writeInt(selectedPawns.size());
        for (Integer selectedPawn : selectedPawns) {
            buffer.writeInt(selectedPawn);
        }
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public BlockPos getStart() {
        return start;
    }

    public BlockPos getEnd() {
        return end;
    }

    public HitResult getHitResult() {
        return hitResult;
    }

    public ServerSelectionType getSelectionType() {
        return ServerSelectionType.valueOf(selectionType);
    }

    @Override
    public void handle(@NotNull MinecraftServer server, @NotNull ServerPlayerEntity player) {
        final var provider = getManagersProvider(player);
        final var taskType = this.getTaskType();
        final var startingBlock = this.getStart();
        final var endingBlock = this.getEnd();
        final var hitResult = this.getHitResult();
        final var selectionType = this.getSelectionType();
        final var tasksCreator = provider.getTasksCreator();
        final var task = tasksCreator.createSelectionTask(taskType, startingBlock, endingBlock, selectionType, hitResult, positions, player);

        if (task.getTaskType() == TaskType.REMOVE) {
            final var buildingsManager = getManagersProvider(player).getBuildingsManager();
            final boolean intersectsWithABuilding = task.toTaskInformationDto().positions().stream()
                    .anyMatch(buildingsManager::isPartOfAnyBuilding);

            if (intersectsWithABuilding) {
                player.sendMessage(Text.of("Can't execute task. Targeted blocks intersect with one of the buildings. Use building menu to destroy the building"));
                return;
            }
        }

        provider.getTaskManager().addTask(task, selectedPawns, player);
    }
}
