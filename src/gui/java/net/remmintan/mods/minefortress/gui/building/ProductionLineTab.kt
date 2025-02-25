package net.remmintan.mods.minefortress.gui.building

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.remmintan.mods.minefortress.gui.building.handlers.IProductionLineTabHandler

class ProductionLineTab(
    private val handler: IProductionLineTabHandler,
    private val textRenderer: TextRenderer
) : ResizableTab {

    override var x: Int = 0
    override var y: Int = 0
    override var backgroundWidth: Int = 0
    override var backgroundHeight: Int = 0

    private var switchToSurvivalButton: ButtonWidget? = null

    fun render(context: DrawContext, mouseX: Int, mouseY: Int) {
        if (handler.isCampfire()) {
            renderCampfireOptions(context, mouseX, mouseY)
        }
    }

    private fun renderCampfireOptions(context: DrawContext, mouseX: Int, mouseY: Int) {
        // Create the button if it doesn't exist
        if (switchToSurvivalButton == null) {
            switchToSurvivalButton = ButtonWidget.builder(
                Text.literal("Explore your village!"),
                { handler.switchToSurvivalMode() }
            )
                .dimensions(0, 0, 200, 20)
                .build()
        } else {
            // Update button position when screen is resized
            switchToSurvivalButton!!.setPosition(backgroundWidth / 2 - 100, 40)
        }

        // Render the button
        val (translatedMouseX, translatedMouseY) = context.matrices.translateMousePosition(mouseX, mouseY)
        switchToSurvivalButton!!.render(context, translatedMouseX, translatedMouseY, 0f)
    }

    fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (handler.isCampfire() && switchToSurvivalButton != null) {
            if (switchToSurvivalButton!!.isHovered) {
                switchToSurvivalButton!!.onClick(mouseX, mouseY)
                return true
            }
        }
        return false
    }

    fun tick() {
        // No tick logic needed for now
    }
} 