package net.remmintan.mods.minefortress.gui.building

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.remmintan.mods.minefortress.gui.building.BuildingScreen.Companion.HEADINGS_COLOR
import net.remmintan.mods.minefortress.gui.building.BuildingScreen.Companion.PRIMARY_COLOR
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
        } else {
            context.drawText(
                textRenderer,
                "No production options available for this building",
                10,
                30,
                PRIMARY_COLOR,
                false
            )
        }
    }

    private fun renderCampfireOptions(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.drawText(textRenderer, "Campfire Options", 10, 30, HEADINGS_COLOR, false)

        if (switchToSurvivalButton == null) {
            switchToSurvivalButton = ButtonWidget.builder(
                Text.literal("Switch to First Person Mode"),
                { handler.switchToSurvivalMode() }
            )
                .dimensions(x + 10, y + 50, 200, 20)
                .build()
        } else {
            switchToSurvivalButton!!.setPosition(x + 10, y + 50)
        }

        switchToSurvivalButton!!.render(context, mouseX, mouseY, 0f)
    }

    fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (handler.isCampfire() && switchToSurvivalButton != null) {
            if (switchToSurvivalButton!!.isMouseOver(mouseX, mouseY)) {
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