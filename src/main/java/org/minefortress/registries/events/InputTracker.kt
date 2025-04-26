package org.minefortress.registries.events

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient


object InputTracker {
    // Variables to store the state from the *previous* tick
    private var wasLeftMouseButtonPressedLastTick = false
    private var wasRightMouseButtonPressedLastTick = false

    // Internal variables to hold the state captured at the end of the current tick
    private var currentLeftMousePressed = false
    private var currentRightMousePressed = false


    fun initialize() {
        // Register to run code at the end of every client tick
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { onClientTickEnd(it) })
    }

    private fun onClientTickEnd(client: MinecraftClient) {
        // This runs *after* the tick processing is mostly done.
        // The 'current' state we read here will become the 'last tick' state
        // *before* the next tick starts.

        // First, update the "last tick" state with the values we captured at the end of the previous tick.

        wasLeftMouseButtonPressedLastTick = currentLeftMousePressed
        wasRightMouseButtonPressedLastTick = currentRightMousePressed

        // Now, capture the *current* state for this tick using the keybinds.
        // This state will be used in the *next* tick as the "last tick" state.
        if (client.options != null) {
            // Check the keybinds associated with mouse buttons
            currentLeftMousePressed = client.options.attackKey.isPressed
            currentRightMousePressed = client.options.useKey.isPressed
        } else {
            // Reset if options are somehow null (e.g., during startup/shutdown)
            currentLeftMousePressed = false
            currentRightMousePressed = false
        }
    }

    /**
     * Checks if the left mouse button (Attack/Destroy keybind) was pressed
     * during the previous client tick.
     *
     * @return true if the left mouse button was pressed last tick, false otherwise.
     */
    fun wasLeftMouseButtonPressedLastTick(): Boolean {
        // Returns the state stored from the end of the previous tick
        return wasLeftMouseButtonPressedLastTick
    }

    /**
     * Checks if the right mouse button (Use Item/Place Block keybind) was pressed
     * during the previous client tick.
     *
     * @return true if the right mouse button was pressed last tick, false otherwise.
     */
    fun wasRightMouseButtonPressedLastTick(): Boolean {
        // Returns the state stored from the end of the previous tick
        return wasRightMouseButtonPressedLastTick
    }

    val isLeftMouseButtonPressedThisTick: Boolean
        /**
         * Checks if the left mouse button (Attack/Destroy keybind) is pressed
         * during the *current* client tick. Useful for comparison.
         *
         * @return true if the left mouse button is pressed now, false otherwise.
         */
        get() {
            val client = MinecraftClient.getInstance()
            if (client?.options != null) {
                return client.options.attackKey.isPressed
            }
            return false
        }

    val isRightMouseButtonPressedThisTick: Boolean
        /**
         * Checks if the right mouse button (Use Item/Place Block keybind) is pressed
         * during the *current* client tick. Useful for comparison.
         *
         * @return true if the right mouse button is pressed now, false otherwise.
         */
        get() {
            val client = MinecraftClient.getInstance()
            if (client?.options != null) {
                return client.options.useKey.isPressed
            }
            return false
        }
}