package net.remmintan.mods.minefortress.gui.building

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.remmintan.mods.minefortress.core.utils.CoreModUtils
import net.remmintan.mods.minefortress.gui.building.handlers.IWorkforceTabHandler
import net.remmintan.mods.minefortress.gui.widget.*

class WorkforceTab(private val handler: IWorkforceTabHandler, private val textRenderer: TextRenderer) : ResizableTab {

    override var x = 0
    override var y = 0
    override var backgroundWidth = 0
    override var backgroundHeight = 0

    private val hireButtons = mutableListOf<HireButtonWithInfo>()
    private val drawables = mutableListOf<Drawable>()

    fun init() {
        drawables.clear()
        hireButtons.clear()

        val professions = handler.getProfessions()
        val rowY = y + 40
        val leftX = x + 10
        val rightX = x + backgroundWidth - 10

        for (i in professions.indices) {
            addNewRow(professions[i], rowY + i * 30, leftX, rightX)
        }
    }

    fun tick() {
        for ((button, costs, profId) in hireButtons) {
            val enoughPlaceForNew =
                (handler.getCurrentCount(profId) + handler.getHireQueue(profId)) < handler.getMaxCount(profId)
            button.active = costs.isEnough && CoreModUtils.getProfessionManager().freeColonists > 0 && enoughPlaceForNew
        }
    }

    fun render(context: DrawContext, mouseX: Int, mouseY: Int) {
        drawables.forEach { it.render(context, mouseX, mouseY, 0f) }
    }

    fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return hireButtons.any { it.button.mouseClicked(mouseX, mouseY, button) }
    }

    private fun addNewRow(profId: String, rowY: Int, leftX: Int, rightX: Int) {
        this.addDrawable(
            ProgressArrowWidget(
                rightX - 48,
                rowY
            ) { handler.getHireProgress(profId) }
        )
        val professionName = ProfessionNameWidget(
            handler.getProfessionName(profId),
            leftX + 10,
            rowY + textRenderer.fontHeight / 2 + 3
        )
        this.addDrawable(professionName)
        val costsWidget = CostsWidget(
            leftX + professionName.offset + 15,
            rowY,
            handler.getCost(profId)
        )
        this.addDrawable(costsWidget)
        val hireButton = ButtonWidget.builder(
            Text.literal("+")
        ) { btn: ButtonWidget? ->
            if (handler.canIncreaseAmount(costsWidget.costs, profId)) {
                handler.increaseAmount(profId)
            }
        }
            .dimensions(rightX - 100, rowY, 20, 20)
            .build()

        this.addDrawable(hireButton)
        hireButtons.add(HireButtonWithInfo(hireButton, costsWidget, profId))
        this.addDrawable(
            ProfessionQueueWidget(
                rightX - 80,
                rowY
            ) { handler.getHireQueue(profId) }
        )

        this.addDrawable(
            ProfessionAmountWidget(
                rightX - 35,
                rowY,
                handler.getProfessionItem(profId),
                { handler.getCurrentCount(profId) },
                { handler.getMaxCount(profId) }
            )
        )
    }

    private fun addDrawable(drawable: Drawable) = drawables.add(drawable)

    private data class HireButtonWithInfo(val button: ButtonWidget, val costs: CostsWidget, val profId: String)

}