package org.minefortress.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import org.minefortress.entity.Colonist

/**
 * Fired when pawns starts doing some task, but they are hungry and there is no food in inventory.
 */

fun interface HungryPawnStartsWorkingCallback {

    fun hungryPawnStartsWorking(pawn: Colonist, foodLevel: Int)

    companion object {
        @JvmField
        val EVENT: Event<HungryPawnStartsWorkingCallback> = EventFactory
            .createArrayBacked(HungryPawnStartsWorkingCallback::class.java) { listeners ->
                HungryPawnStartsWorkingCallback { pawn, foodLevel ->
                    listeners.forEach { it.hungryPawnStartsWorking(pawn, foodLevel) }
                }
            }
    }

}