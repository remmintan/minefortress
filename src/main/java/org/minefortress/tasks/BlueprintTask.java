package org.minefortress.tasks;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.minefortress.blueprints.data.BlueprintBlockData;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressBedInfo;
import org.minefortress.fortress.FortressBulding;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.tasks.block.info.BlockStateTaskBlockInfo;
import org.minefortress.tasks.block.info.TaskBlockInfo;

import java.util.*;

public class BlueprintTask extends AbstractTask {

    private final Map<BlockPos, BlockState> blueprintData;
    private final Map<BlockPos, BlockState> blueprintEntityData;
    private final Map<BlockPos, BlockState> blueprintAutomaticData;
    private final float floorLevel;
    private final String requirementId;

    private final Set<FortressBedInfo> beds;

    public BlueprintTask(
            UUID id,
            BlockPos startingPos,
            BlockPos endingPos,
            Map<BlockPos, BlockState> blueprintData,
            Map<BlockPos, BlockState> blueprintEntityData,
            Map<BlockPos, BlockState> blueprintAutomaticData,
            int floorLevel,
            String requirementId
    ) {
        super(id, TaskType.BUILD, startingPos, endingPos);
        this.blueprintData = blueprintData;
        this.blueprintEntityData = blueprintEntityData;
        this.blueprintAutomaticData = blueprintAutomaticData;
        this.floorLevel = floorLevel;

        Set<FortressBedInfo> allBeds = new HashSet<>();

        for (Map.Entry<BlockPos, BlockState> entry : blueprintAutomaticData.entrySet()) {
            BlockState state = entry.getValue();
            if (state.isIn(BlockTags.BEDS)) {
                final BedPart bedPart = state.get(BedBlock.PART);
                if (bedPart == BedPart.HEAD) {
                    allBeds.add(new FortressBedInfo(entry.getKey().add(startingBlock)));
                }
            }
        }

        this.beds = Collections.unmodifiableSet(allBeds);
        this.requirementId = requirementId;
    }

    @Override
    public TaskPart getNextPart(ServerWorld level) {
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
    public void finishPart(TaskPart part, Colonist colonist) {
        final ServerWorld world = (ServerWorld) colonist.world;
        if(parts.isEmpty() && getCompletedParts()+1 >= totalParts) {
            blueprintEntityData.forEach((pos, state) -> {
                world.setBlockState(pos.add(startingBlock), state, 3);
                final var item = state.getBlock().asItem();
                final var fortressManagerOpt = colonist.getFortressManager();
                if(fortressManagerOpt.isPresent()) {
                    final var fortressServerManager = fortressManagerOpt.get();
                    if(fortressServerManager.isSurvival()) {
                        if (BlueprintBlockData.IGNORED_ITEMS.contains(item)) {
                            fortressServerManager
                                    .getServerResourceManager()
                                    .removeItemIfExists(item);
                        } else {
                            fortressServerManager
                                    .getServerResourceManager()
                                    .removeReservedItem(this.getId(), item);
                        }
                    }
                }
            });

            if(blueprintAutomaticData != null)
                blueprintAutomaticData
                        .forEach((pos, state) -> {
                            world.setBlockState(pos.add(startingBlock), state, 3);
                            colonist.doActionOnMasterPlayer(player -> player
                                    .getFortressServerManager()
                                    .getServerResourceManager()
                                    .removeReservedItem(this.getId(), state.getBlock().asItem()));
                        });


            colonist.doActionOnMasterPlayer(player -> {
                final FortressServerManager fortressServerManager = player.getFortressServerManager();
                final FortressBulding fortressBulding = new FortressBulding(startingBlock, endingBlock, beds, requirementId);
                fortressServerManager.addBuilding(fortressBulding);
            });
        }
        super.finishPart(part, colonist);
    }

    private Item getItemFromState(BlockState state) {
        final Block block = state.getBlock();
        return block.asItem();
    }

}
