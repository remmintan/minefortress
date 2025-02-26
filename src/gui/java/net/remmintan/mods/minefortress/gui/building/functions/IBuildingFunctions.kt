package net.remmintan.mods.minefortress.gui.building.functions

import net.minecraft.client.gui.DrawContext

/**
 * Interface for building-specific functions that can be rendered in the Production Line tab.
 * Each building type can have its own implementation of this interface.
 */
interface IBuildingFunctions {
    /**
     * Render the building-specific functions
     */
    fun render(context: DrawContext, x: Int, y: Int, width: Int, height: Int, mouseX: Int, mouseY: Int)

    /**
     * Handle mouse clicks on the building-specific functions
     * @return true if the click was handled, false otherwise
     */
    fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean

    /**
     * Update the building-specific functions
     */
    fun tick()
} 