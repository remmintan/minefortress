package org.minefortress.registries.events.client

import kotlinx.atomicfu.locks.synchronized
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.item.Items
import net.remmintan.mods.minefortress.core.isClientInFortressGamemode
import net.remmintan.mods.minefortress.core.utils.CoreModUtils
import org.minefortress.fortress.FortressToast


class ToastEvents {

    private val LOCK = Any()
    private var campfireToast: FortressToast? = null

    fun register() {
        ClientTickEvents.START_CLIENT_TICK.register { client ->
            if (isClientInFortressGamemode() && CoreModUtils.getFortressManager().isCenterNotSet) {
                synchronized(LOCK) {
                    if (campfireToast == null) {
                        campfireToast = FortressToast("Set up your Fortress", "Right-click to place", Items.CAMPFIRE)
                        client.toastManager.add(campfireToast)
                    }
                }
            } else {
                synchronized(LOCK) {
                    campfireToast?.let {
                        it.hide()
                        campfireToast = null
                    }
                }
            }
        }
    }
}