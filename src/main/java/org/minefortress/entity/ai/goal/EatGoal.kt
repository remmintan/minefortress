package org.minefortress.entity.ai.goal

import net.minecraft.entity.ai.goal.Goal
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IEatControl
import net.remmintan.mods.minefortress.core.interfaces.resources.server.IServerFoodManager
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import org.minefortress.entity.HungryEntity

class EatGoal(private val entity: HungryEntity) : Goal() {
    override fun canStart(): Boolean {
        return eatControl.isHungry && foodManager.hasFood()
    }

    override fun start() {
        foodManager.getFood()?.let {
            eatControl.eatFood(it.item)
        }
    }

    override fun shouldContinue(): Boolean {
        return super.shouldContinue() && eatControl.isEating
    }

    private val foodManager: IServerFoodManager by lazy {
        val server = entity.server!!
        require(entity is IFortressAwareEntity)

        server.getManagersProvider(entity.fortressPos!!).foodManager
    }

    private val eatControl: IEatControl
        get() = entity.eatControl.orElseThrow()
}
