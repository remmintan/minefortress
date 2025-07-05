package org.minefortress.entity.ai

import baritone.api.IBaritone
import baritone.api.event.events.PathEvent
import baritone.api.event.listener.AbstractGameEventListener
import baritone.api.pathing.goals.GoalNear
import baritone.api.utils.BetterBlockPos
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.ModLogger
import org.minefortress.entity.Colonist
import kotlin.math.floor


class MovementHelper(private val colonist: Colonist) {
    private val baritone: IBaritone = colonist.baritone
    var goal: BlockPos? = null
        private set
    private var distance: Double = 0.0

    private var stuckTicks = 0
    var isStuck: Boolean = false
        private set
    private var lastPos: BlockPos? = null

    init {
        baritone.gameEventHandler.registerEventListener(StuckOnFailEventListener())
    }

    fun reset() {
        val hasWorkGoal = goal != null
        val tryingToReachGoal = stillTryingToReachGoal()
        ModLogger.LOGGER.debug(
            "{} movement helper reset [has work goal: {}, trying to reach the goal {}]",
            colonistName, hasWorkGoal, tryingToReachGoal
        )
        this.goal = null
        this.distance = 0.0
        this.lastPos = null
        this.stuckTicks = 0
        this.isStuck = false
        colonist.navigation.stop()
        baritone.pathingBehavior.cancelEverything()
        baritone.followProcess.cancel()
        val settings = baritone.settings()
        settings.allowParkour.set(true)
        settings.maxFallHeightBucket.set(1000)
    }

    private val colonistName: String
        get() = colonist.name.string

    @JvmOverloads
    fun goTo(
        goal: BlockPos,
        speed: Float = colonist.adjustedMovementSpeed,
        reachDistance: Double = Colonist.WORK_REACH_DISTANCE.toDouble()
    ) {
        if (this.goal != null && this.goal == goal) {
            ModLogger.LOGGER.debug("{} trying to set new goal, but current goal is the same", colonistName)
            return
        }
        ModLogger.LOGGER.debug("{} set new goal {}. speed: {}", colonistName, goal, speed)
        this.reset()
        this.goal = goal
        this.distance = floor(reachDistance)
        if (this.goal == null) return
        colonist.movementSpeed = speed
        if (this.hasReachedGoal()) {
            ModLogger.LOGGER.debug("{} the goal {} is already reached", colonistName, goal)
            return
        }
        if (colonist.isSleeping) {
            colonist.wakeUp()
        }

        baritone.customGoalProcess.setGoalAndPath(GoalNear(this.goal, this.distance.toInt() - 1))
    }

    fun follow(entity: LivingEntity, speed: Float) {
        this.reset()
        baritone.settings().followRadius.set(1)
        colonist.movementSpeed = speed
        baritone.followProcess.follow { it: Entity -> it == entity }
    }

    fun hasReachedGoal(): Boolean {
        return goal?.let {
            it.isWithinDistance(colonist.pos, distance) && !baritone.pathingBehavior.isPathing
        } ?: false
    }

    fun tick() {
        if (goal == null) return

        val currentPos = colonist.blockPos
        if (!hasReachedGoal() && currentPos == lastPos) {
            stuckTicks++
            ModLogger.LOGGER.debug(
                "{} on the same place without reaching the goal for {} ticks. Goal: {}",
                colonistName,
                stuckTicks,
                goal
            )
            if (stuckTicks > 20) {
                ModLogger.LOGGER.debug(
                    "{} on the same place for too long. Setting stuck to true. Goal: {}",
                    colonistName,
                    goal
                )
                isStuck = true
            }
        } else {
            stuckTicks = 0
        }
        lastPos = currentPos
    }

    fun stillTryingToReachGoal(): Boolean {
        return baritone.pathingBehavior.isPathing
    }

    private inner class StuckOnFailEventListener : AbstractGameEventListener {
        private var lastDestination: BlockPos? = null
        private var stuckCounter = 0

        override fun onPathEvent(pathEvent: PathEvent) {
            if (pathEvent == PathEvent.AT_GOAL && !hasReachedGoal()) {
                ModLogger.LOGGER.debug(
                    "{} signaling at goal without actually reaching the goal {}. Setting stuck to true",
                    colonistName,
                    goal
                )
                isStuck = true
            }

            if (pathEvent == PathEvent.CALC_FINISHED_NOW_EXECUTING) {
                val dest = baritone.pathingBehavior.path.map { it.dest }.orElse(BetterBlockPos.ORIGIN)
                if (lastDestination != null) {
                    if (dest == lastDestination) {
                        stuckCounter++
                        ModLogger.LOGGER.debug(
                            "{} Calculated destination is the same as previous for {} ticks (going in circles). [Goal: {}]",
                            colonistName, stuckCounter, goal
                        )
                        if (stuckCounter > 3) {
                            ModLogger.LOGGER.debug(
                                "{} going in circles for too much time {} [goal: {}]",
                                colonistName, stuckCounter, goal
                            )
                            isStuck = true
                            stuckCounter = 0
                            lastDestination = null
                            baritone.pathingBehavior.cancelEverything()
                        }
                    } else {
                        stuckCounter = 0
                    }
                }
                lastDestination = dest
            }

            if (pathEvent == PathEvent.CALC_FAILED) {
                ModLogger.LOGGER.debug("{} can't find path to {}", colonistName, goal)
                isStuck = true
            }
        }
    }
}
