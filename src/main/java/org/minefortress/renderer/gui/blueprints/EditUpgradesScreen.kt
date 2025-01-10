package org.minefortress.renderer.gui.blueprints

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.remmintan.mods.minefortress.gui.widget.BlueprintUpgradeSlot
import org.minefortress.renderer.gui.blueprints.handler.EditUpgradesScreenHandler

private const val BACKGROUND_WIDTH = 248
private const val BACKGROUND_HEIGHT = 166
private const val SLOT_X = BACKGROUND_WIDTH / 2 - (44 * 3 + 4 * 2) / 2
private const val SLOT_Y = 50

class EditUpgradesScreen(private val handler: EditUpgradesScreenHandler) : Screen(Text.of("Select level to edit")) {

    companion object {
        val BACKGROUND_TEXTURE = Identifier.of("minefortress", "textures/gui/demo_background.png")
    }

    private var x: Int = 0
    private var y: Int = 0
    private var selectedSlot: BlueprintUpgradeSlot? = null
    private val btnWidget: ButtonWidget

    init {
        btnWidget = ButtonWidget.Builder(Text.of("Back")) { close() }
            .width(100)
            .build()
    }

    private val upgradeSlots: List<BlueprintUpgradeSlot> by lazy {
        handler.upgrades.mapIndexed { index, slot ->
            BlueprintUpgradeSlot.create(SLOT_X, SLOT_Y, index, 0, 0, slot, this.textRenderer, false)
        }
    }

    override fun init() {
        this.x = (this.width - BACKGROUND_WIDTH) / 2
        this.y = (this.height - BACKGROUND_HEIGHT) / 2

        btnWidget.setPosition(x + BACKGROUND_WIDTH / 2 - 50, y + BACKGROUND_HEIGHT - 40)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        context ?: return
        this.renderBackground(context, mouseX, mouseY, delta)

        val matrices = context.matrices
        matrices.push()
        matrices.translate(x.toDouble(), y.toDouble(), 0.0)

        context.drawTexture(BACKGROUND_TEXTURE, 0, 0, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT)

        val headingText = Text.of("Select which level to edit:")
        val headingTextLength = this.textRenderer.getWidth(headingText)
        context.drawText(textRenderer, headingText, (BACKGROUND_WIDTH - headingTextLength) / 2, 25, 0x404040, false)

        this.selectedSlot = null
        this.upgradeSlots.forEach {
            it.render(context, mouseX, mouseY, delta)
            if (it.hovered) {
                this.selectedSlot = it
            }
        }

        matrices.pop()

        val matrixStack = RenderSystem.getModelViewStack()
        matrixStack.push()
        matrixStack.translate(x.toDouble() + SLOT_X + 5, y.toDouble() + SLOT_Y + 9, 0.0)
        RenderSystem.applyModelViewMatrix()

        upgradeSlots.forEach { it.renderUpgradeBlueprint() }

        matrixStack.pop()
        RenderSystem.applyModelViewMatrix()

        btnWidget.render(context, mouseX, mouseY, delta)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val parentResult = super.mouseReleased(mouseX, mouseY, button)

        selectedSlot?.let {
            handler.edit.accept(it.slot.metadata)
            return true
        }

        if (button == 0)
            btnWidget.onClick(mouseX, mouseY)

        return parentResult
    }

    override fun close() {
        this.client?.setScreen(BlueprintsScreen())
    }
}