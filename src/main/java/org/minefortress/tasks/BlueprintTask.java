package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.TaskType;
import net.remmintan.mods.minefortress.core.dtos.tasks.TaskInformationDto;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWorkerPawn;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskBlockInfo;
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITaskPart;
import net.remmintan.mods.minefortress.core.utils.ServerExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;

import java.util.*;

public class BlueprintTask extends AbstractTask {

    private final Map<BlockPos, BlockState> blueprintData;
    private final Map<BlockPos, BlockState> blueprintEntityData;
    private final Map<BlockPos, BlockState> blueprintAutomaticData;
    private final int floorLevel;
    private final String blueprintId;

    public BlueprintTask(
            UUID id,
            BlockPos startingPos,
            BlockPos endingPos,
            Map<BlockPos, BlockState> blueprintData,
            Map<BlockPos, BlockState> blueprintEntityData,
            Map<BlockPos, BlockState> blueprintAutomaticData,
            int floorLevel,
            @NotNull String blueprintId
    ) {
        super(id, TaskType.BUILD, startingPos, endingPos);
        this.blueprintData = blueprintData;
        this.blueprintEntityData = blueprintEntityData;
        this.blueprintAutomaticData = blueprintAutomaticData;
        this.floorLevel = floorLevel;
        this.blueprintId = blueprintId;
    }

    @Override
    public ITaskPart getNextPart(IWorkerPawn colonist) {
        final Pair<BlockPos, BlockPos> partStartAndEnd = parts.poll();
        List<ITaskBlockInfo> blockInfos = getTaskBlockInfos(partStartAndEnd);
        return new TaskPart(partStartAndEnd, blockInfos, this);
    }

    @NotNull
    private List<ITaskBlockInfo> getTaskBlockInfos(Pair<BlockPos, BlockPos> partStartAndEnd) {
        final BlockPos start = partStartAndEnd.getFirst();
        final BlockPos delta = start.subtract(startingBlock);
        final Iterable<BlockPos> allPositionsInPart = BlockPos.iterate(start, partStartAndEnd.getSecond());

        List<ITaskBlockInfo> blockInfos = new ArrayList<>();
        for (BlockPos pos : allPositionsInPart) {
            final BlockState state = blueprintData.getOrDefault(pos.subtract(start).add(delta), pos.subtract(start).add(delta).getY()<floorLevel?Blocks.DIRT.getDefaultState():Blocks.AIR.getDefaultState());
            if(state.isAir()) continue;
            final BlockStateTaskBlockInfo blockStateTaskBlockInfo = new BlockStateTaskBlockInfo(getItemFromState(state), pos.toImmutable(), state);
            blockInfos.add(blockStateTaskBlockInfo);
        }
        return blockInfos;
    }

    @Override
    public void finishPart(ITaskPart part, IWorkerPawn worker) {
        final ServerWorld world = worker.getServerWorld();
        if(parts.isEmpty() && getCompletedParts()+1 >= totalParts) {
            if(blueprintEntityData != null) {
                blueprintEntityData.forEach((pos, state) -> {
                    final var realPos = pos.add(startingBlock);
                    world.setBlockState(realPos, state, 3);
                    final var item = state.getBlock().asItem();
                    removeReservedItem(worker, item);
                });
            }

            if(blueprintAutomaticData != null) {
                blueprintAutomaticData
                    .forEach((pos, state) -> {
                        final var realpos = pos.add(startingBlock);
                        world.setBlockState(realpos, state, 3);

                        if(!state.isIn(BlockTags.BEDS) || state.get(BedBlock.PART) != BedPart.FOOT) {
                            removeReservedItem(worker, state.getBlock().asItem());
                        }
                    });
            }

            final var mergeBlockData = new HashMap<>(blueprintData);
            if(blueprintEntityData != null) mergeBlockData.putAll(blueprintEntityData);
            if(blueprintAutomaticData != null) mergeBlockData.putAll(blueprintAutomaticData);


            final var provider = ServerModUtils.getManagersProvider(worker);

            throw new NotImplementedException("send blueprint metadata together with task");
//            final var metadata = provider.getBlueprintManager().get(blueprintId);
//            final var buildingManager = provider.getBuildingsManager();
//            buildingManager.addBuilding(worker.getFortressPos(), metadata, startingBlock, endingBlock, mergeBlockData);
        }
        super.finishPart(part, worker);
    }

    private void removeReservedItem(IFortressAwareEntity worker, Item item) {
        final var provider = ServerModUtils.getManagersProvider(worker);
        if (ServerExtensionsKt.isSurvivalFortress(worker.getServer())) {
            final var resourceManager = provider
                    .getResourceManager();
            if (SimilarItemsHelper.isIgnorable(item)) {
                resourceManager
                        .removeItemIfExists(this.getId(), item);
            } else {
                resourceManager
                        .removeReservedItem(this.getId(), item);
            }
        }
    }

    @Override
    public List<TaskInformationDto> toTaskInformationDto() {
        Set<BlockPos> allBlocks = new HashSet<>(blueprintData.keySet());
        if(blueprintEntityData != null) allBlocks.addAll(blueprintEntityData.keySet());
        if(blueprintAutomaticData != null) allBlocks.addAll(blueprintAutomaticData.keySet());
        final var positions = allBlocks.stream().map(it -> it.add(startingBlock)).toList();
        return List.of(new TaskInformationDto(id, positions, taskType));
    }
}
