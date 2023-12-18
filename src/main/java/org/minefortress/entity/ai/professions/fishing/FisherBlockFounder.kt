package org.minefortress.entity.ai.professions.fishing

import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import org.minefortress.entity.Colonist
import java.util.*


const val SEARCH_RADIUS = 20

fun getFisherGoal(pawn: Colonist, pivotBlock: BlockPos, predicate: (BlockPos) -> Boolean): Optional<FisherGoal> {
    val world = pawn.world
    val randomBlock = BlockPos.iterateRandomly(world.random, SEARCH_RADIUS*3, pivotBlock, 1).first()

    return BlockPos
            .findClosest(randomBlock, SEARCH_RADIUS*3, SEARCH_RADIUS*3) {
                predicate(it)
            }
            .map {
                it.toImmutable()
            }
            .flatMap {
                getFisherGoalFromWaterPos(it, pawn)
            }
}

private fun getFisherGoalFromWaterPos(waterPos: BlockPos, pawn: Colonist): Optional<FisherGoal> {
    val world = pawn.world
    return BlockPos
            .findClosest(waterPos, SEARCH_RADIUS, SEARCH_RADIUS) {
                val immutablePos = it.toImmutable()
                val state = world.getBlockState(immutablePos)
                val aboveState = world.getBlockState(immutablePos.up())
                val aboveState2 = world.getBlockState(immutablePos.up(2))

                !state.isAir && aboveState.isAir && aboveState2.isAir&& !state.isOf(Blocks.WATER)
            }
            .map {
                FisherGoal(waterPos, it)
            }
}