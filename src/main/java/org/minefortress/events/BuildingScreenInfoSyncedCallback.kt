package org.minefortress.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

fun interface BuildingScreenInfoSyncedCallback {

    fun buildingScreenInfoSynced()

    companion object {
        @JvmField
        val EVENT: Event<BuildingScreenInfoSyncedCallback> = EventFactory
            .createArrayBacked(BuildingScreenInfoSyncedCallback::class.java) { listeners ->
                BuildingScreenInfoSyncedCallback {
                    listeners.forEach { it.buildingScreenInfoSynced() }
                }
            }
    }

}