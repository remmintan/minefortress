package net.remmintan.mods.minefortress.gui.building

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.remmintan.mods.minefortress.gui.building.functions.IBuildingFunctions
import net.remmintan.mods.minefortress.gui.building.handlers.IProductionLineTabHandler

class ProductionLineTab(
    private val handler: IProductionLineTabHandler,
    private val textRenderer: TextRenderer
) : ResizableTab {

    override var x: Int = 0
    override var y: Int = 0
    override var backgroundWidth: Int = 0
    override var backgroundHeight: Int = 0

    private var buildingFunctions: IBuildingFunctions? = null

    fun render(context: DrawContext, mouseX: Int, mouseY: Int) {
        if (handler.hasFunctions()) {
            // Lazy initialize the building functions
            if (buildingFunctions == null) {
                buildingFunctions = handler.getBuildingFunctions(textRenderer)
            }

            // Render the building functions
            buildingFunctions?.render(context, x, y, backgroundWidth, backgroundHeight, mouseX, mouseY)
        }
    }

    fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return buildingFunctions?.onMouseClicked(mouseX, mouseY, button) ?: false
    }

    fun tick() {
        buildingFunctions?.tick()
    }
} 
