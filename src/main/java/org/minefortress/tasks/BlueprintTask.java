package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.interfaces.IFortressAwareEntity;
import org.minefortress.entity.interfaces.IWorkerPawn;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.fortress.buildings.FortressBuilding;
import org.minefortress.fortress.buildings.FortressBuildingManager;
import org.minefortress.fortress.resources.SimilarItemsHelper;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.TaskBlockInfo;

import java.util.*;

public class BlueprintTask extends AbstractTask {

    private final Map<BlockPos, BlockState> blueprintData;
    private final Map<BlockPos, BlockState> blueprintEntityData;
    private final Map<BlockPos, BlockState> blueprintAutomaticData;
    private final int floorLevel;
    private final String requirementId;
    private final String blueprintId;

    public BlueprintTask(
            UUID id,
            BlockPos startingPos,
            BlockPos endingPos,
            Map<BlockPos, BlockState> blueprintData,
            Map<BlockPos, BlockState> blueprintEntityData,
            Map<BlockPos, BlockState> blueprintAutomaticData,
            int floorLevel,
            String requirementId,
            @NotNull String blueprintId
    ) {
        super(id, TaskType.BUILD, startingPos, endingPos);
        this.blueprintData = blueprintData;
        this.blueprintEntityData = blueprintEntityData;
        this.blueprintAutomaticData = blueprintAutomaticData;
        this.floorLevel = floorLevel;
        this.requirementId = requirementId;
        this.blueprintId = blueprintId;
    }

    @Override
    public TaskPart getNextPart(ServerWorld level, IWorkerPawn colonist) {
        final Pair<BlockPos, BlockPos> partStartAndEnd = parts.poll();
        List<TaskBlockInfo> blockInfos = getTaskBlockInfos(partStartAndEnd);
        return new TaskPart(partStartAndEnd, blockInfos, this);
    }

    @NotNull
    private List<TaskBlockInfo> getTaskBlockInfos(Pair<BlockPos, BlockPos> partStartAndEnd) {
        final BlockPos start = partStartAndEnd.getFirst();
        final BlockPos delta = start.subtract(startingBlock);
        final Iterable<BlockPos> allPositionsInPart = BlockPos.iterate(start, partStartAndEnd.getSecond());

        List<TaskBlockInfo> blockInfos = new ArrayList<>();
        for (BlockPos pos : allPositionsInPart) {
            final BlockState state = blueprintData.getOrDefault(pos.subtract(start).add(delta), pos.subtract(start).add(delta).getY()<floorLevel?Blocks.DIRT.getDefaultState():Blocks.AIR.getDefaultState());
            if(state.isAir()) continue;
            final BlockStateTaskBlockInfo blockStateTaskBlockInfo = new BlockStateTaskBlockInfo(getItemFromState(state), pos.toImmutable(), state);
            blockInfos.add(blockStateTaskBlockInfo);
        }
        return blockInfos;
    }

    @Override
    public void finishPart(TaskPart part, IWorkerPawn colonist) {
        final ServerWorld world = colonist.getServerWorld();
        if(parts.isEmpty() && getCompletedParts()+1 >= totalParts) {
            if(blueprintEntityData != null) {
                blueprintEntityData.forEach((pos, state) -> {
                    final var realPos = pos.add(startingBlock);
                    world.setBlockState(realPos, state, 3);
                    final var item = state.getBlock().asItem();
                    removeReservedItem(colonist, item);
                    addSpecialBlueprintBlock(colonist, state.getBlock(), realPos);
                });
            }

            if(blueprintAutomaticData != null) {
                blueprintAutomaticData
                    .forEach((pos, state) -> {
                        final var realpos = pos.add(startingBlock);
                        world.setBlockState(realpos, state, 3);

                        addSpecialBlueprintBlock(colonist, state.getBlock(), realpos);
                        if(!state.isIn(BlockTags.BEDS) || state.get(BedBlock.PART) != BedPart.FOOT) {
                            removeReservedItem(colonist, state.getBlock().asItem());
                        }
                    });
            }

            final var mergeBlockData = new HashMap<>(blueprintData);
            if(blueprintEntityData != null) mergeBlockData.putAll(blueprintEntityData);
            if(blueprintAutomaticData != null) mergeBlockData.putAll(blueprintAutomaticData);

            final FortressBuilding fortressBuilding = new FortressBuilding(
                    UUID.randomUUID(),
                    startingBlock,
                    endingBlock,
                    requirementId,
                    blueprintId,
                    floorLevel,
                    mergeBlockData
            );
            final var serverManager = colonist.getFortressServerManager().orElseThrow();
            serverManager.expandTheVillage(fortressBuilding.getStart());
            serverManager.expandTheVillage(fortressBuilding.getEnd());

            final var buildingManager = serverManager.getFortressBuildingManager();
            buildingManager.addBuilding(fortressBuilding);
        }
        super.finishPart(part, colonist);
    }

    private void addSpecialBlueprintBlock(IWorkerPawn colonist, Block block, BlockPos pos) {
        colonist.getFortressServerManager().orElseThrow().addSpecialBlocks(block, pos, true);
    }

    private void removeReservedItem(IFortressAwareEntity colonist, Item item) {
        final var fortressManager = colonist.getFortressServerManager().orElseThrow();
        if(fortressManager.isSurvival()) {
            if (SimilarItemsHelper.isIgnorable(item)) {
                fortressManager
                        .getServerResourceManager()
                        .removeItemIfExists(this.getId(), item);
            } else {
                fortressManager
                        .getServerResourceManager()
                        .removeReservedItem(this.getId(), item);
            }
        }
    }

}
