package net.remmintan.mods.minefortress.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

private const val TEXT_STRING =
    "Halt, traveler! Your world was shaped in the early days of MineFortress, an older age where some wonders may not serve you well. To experience its full glory, you must forge a new world. Be warned, the past holds no promises!"
private val TEXT = Text.of(TEXT_STRING)

class OutdatedWorldScreen : Screen(Text.of("This world is no longer supported!")) {

    private val btn: ButtonWidget = ButtonWidget.builder(Text.of("A shame, but so be it!")) {
        this.client?.setScreen(null)
    }
        .size(200, 20)
        .build()

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        context?.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 50, 0xFFFFFF)
        context?.drawTextWrapped(this.textRenderer, TEXT, 50, 90, this.width - 100, 0xFFFFFF)

        val textHeight = this.textRenderer.getWrappedLinesHeight(TEXT, this.width - 100)
        btn.x = this.width / 2 - 100
        btn.y = 90 + textHeight + 40

        btn.render(context, mouseX, mouseY, delta)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val mouseReleased = super.mouseReleased(mouseX, mouseY, button)

        if (button == 0) {
            btn.onClick(mouseX, mouseY)
            return true
        }

        return mouseReleased
    }
}