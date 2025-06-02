package org.minefortress.entity.ai.goal

import net.minecraft.util.math.BlockPos
import net.remmintan.mods.minefortress.core.utils.ServerModUtils
import org.minefortress.entity.Colonist
import org.minefortress.entity.colonist.IFortressHungerManager
import java.time.Duration
import java.time.LocalDateTime

class WanderAroundTheFortressGoal(colonist: Colonist) : AbstractFortressGoal(colonist) {
    private var goal: BlockPos? = null
    private var stopTime: LocalDateTime = LocalDateTime.now()
    private var delay: Int = 0

    override fun canStart(): Boolean {
        if (colonist.eatControl.map { it.isEating }.orElse(false)) return false
        if (!isDay || colonist.taskControl.hasTask()) return false
        val durationSinceStop = Duration.between(stopTime, LocalDateTime.now()).toMillis()
        if (durationSinceStop < delay) return false

        goal = ServerModUtils
            .getFortressManager(colonist)
            .map { it.randomFortressPosition }
            .orElse(null)

        return goal != null
    }

    override fun start() {
        colonist.currentTaskDesc = "Wandering around"
        colonist.putItemInHand(null)
        colonist.movementHelper.goTo(goal!!, Colonist.SLOW_MOVEMENT_SPEED)
    }

    override fun tick() {
        super.tick()
        colonist.addHunger(IFortressHungerManager.IDLE_EXHAUSTION)
        if (colonist.movementHelper.isStuck) {
            if (goal != null) {
                colonist.resetControls()
                colonist.teleport(goal!!.x.toDouble(), goal!!.y.toDouble(), goal!!.z.toDouble())
            }
        }
    }

    override fun shouldContinue(): Boolean {
        return isDay && !colonist.taskControl.hasTask() && colonist.movementHelper.stillTryingToReachGoal()
    }

    override fun stop() {
        goal = null
        colonist.movementHelper.reset()
        stopTime = LocalDateTime.now()
        delay = colonist.world.random.nextInt(6000) + 5000
    }

    private val isDay: Boolean
        get() = colonist.world.isDay
}
