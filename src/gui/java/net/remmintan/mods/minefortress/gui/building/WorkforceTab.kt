package net.remmintan.mods.minefortress.gui.building

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.remmintan.mods.minefortress.gui.building.handlers.IWorkforceTabHandler
import net.remmintan.mods.minefortress.gui.widget.*
import kotlin.properties.Delegates

class WorkforceTab(private val handler: IWorkforceTabHandler, private val textRenderer: TextRenderer) : ResizableTab {

    override var x = 0
    override var y = 0
    override var backgroundWidth = 0
    override var backgroundHeight = 0

    private val hireButtons = mutableListOf<Pair<String, HireButtonWidget>>()
    private val drawables = mutableListOf<Drawable>()

    private var initialized: Boolean by Delegates.vetoable(false) { _, _, new -> new }

    fun tick() {
        if (!initialized) {
            init()
            initialized = true
        }
        for ((profId, button) in hireButtons) {
            val canHireMore = handler.canHireMore(profId)
            button.active = canHireMore
            button.tooltip = if (canHireMore) null else Tooltip.of(Text.of("Not enough resources or free colonists"))
        }
    }

    fun render(context: DrawContext, mouseX: Int, mouseY: Int) {
        val recruitUnitsLabel = "Recruit Units"
        context.drawText(this.textRenderer, recruitUnitsLabel, 7, 20, BuildingScreen.PRIMARY_COLOR, false)
        val labelWidth = this.textRenderer.getWidth(recruitUnitsLabel)
        context.drawText(
            this.textRenderer,
            "[Available pawns: ${handler.getAvailablePawns()}]",
            7 + labelWidth + 2,
            20,
            BuildingScreen.SECONDARY_COLOR,
            false
        )

        val (translatedMouseX, translatedMouseY) = context.matrices.translateMousePosition(mouseX, mouseY)
        drawables.forEach { it.render(context, translatedMouseX, translatedMouseY, 0f) }

        context.drawText(this.textRenderer, "Enhance Skills", 7, 100, BuildingScreen.PRIMARY_COLOR, false)
    }

    fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return hireButtons.any { (_, btn) -> btn.mouseClicked(mouseX, mouseY, button) }
    }

    private fun init() {
        drawables.clear()
        hireButtons.clear()

        val professions = handler.getProfessions()
        val rowY = 32
        val leftX = 0
        val rightX = backgroundWidth

        for (i in professions.indices) {
            addNewRow(professions[i], rowY + i * 30, leftX, rightX)
        }
    }

    private fun addNewRow(profId: String, rowY: Int, leftX: Int, rightX: Int) {
        // Name
        val professionName = ProfessionNameWidget(
            handler.getProfessionName(profId),
            leftX + 10,
            rowY + textRenderer.fontHeight / 2 + 3
        )
        this.addDrawable(professionName)

        // Cost
        val cost = handler.getCost(profId)
        val costsWidget = CostsWidget(
            leftX + professionName.offset + 15,
            rowY,
            cost
        )
        this.addDrawable(costsWidget)

        // Hire button
        val hireButton = HireButtonWidget.builder(
            Text.literal("+")
        ) { btn: ButtonWidget? ->
            if (handler.canHireMore(profId)) {
                handler.increaseAmount(profId)
            }
        }
            .dimensions(rightX - 110, rowY, 20, 20)
            .build()
        this.addDrawable(hireButton)
        hireButtons.add(profId to hireButton)

        // Queue
        this.addDrawable(
            ProfessionQueueWidget(
                rightX - 90,
                rowY
            ) { handler.getHireQueue(profId) }
        )

        // Progress arrow
        this.addDrawable(
            ProgressArrowWidget(
                rightX - 58,
                rowY
            ) { handler.getHireProgress(profId) }
        )

        // Amount
        this.addDrawable(
            ProfessionAmountWidget(
                rightX - 45,
                rowY,
                handler.getProfessionItem(profId),
                { handler.getCurrentCount(profId) },
                { handler.getMaxCount(profId) }
            )
        )
    }

    private fun addDrawable(drawable: Drawable) = drawables.add(drawable)
}