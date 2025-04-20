package net.remmintan.mods.minefortress.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.MultilineTextWidget
import net.minecraft.text.Text
import net.remmintan.mods.minefortress.core.config.MineFortressClientConfig

class DisclaimerScreen(
    private val nextScreen: Screen // The screen to open after acknowledgment (CreateWorldScreen)
) : Screen(Text.translatable("minefortress.disclaimer.title")) {

    private val contentStartY = 40
    private val textWidth = 300 // Max width for the message text
    private val buttonWidth = 150
    private val buttonSpacing = 10 // Space above button

    override fun init() {
        super.init()

        val textBlockX = (this.width - textWidth) / 2
        var currentY = contentStartY

        // --- Add Informational Text ---
        // Use Formatting.YELLOW or similar for the URL for emphasis
        val message = Text.translatable("minefortress.disclaimer.message1")
            .append("\n\n") // Add punctuation after URL
            .append(Text.translatable("minefortress.disclaimer.message2"))
            .append("\n\n")
            .append(Text.translatable("minefortress.disclaimer.message3"))
            .append("\n\n")
            .append(Text.translatable("minefortress.disclaimer.message4"))


        val textWidget = MultilineTextWidget(textBlockX, currentY, message, this.textRenderer)
            .setMaxWidth(textWidth)
            .setCentered(false) // Align text left for readability

        this.addDrawableChild(textWidget)
        currentY += textWidget.height + buttonSpacing * 2 // Add extra spacing before button

        // --- Add Acknowledge Button ---
        val buttonX = (this.width - buttonWidth) / 2
        val buttonY = currentY // Position directly below text

        addDrawableChild(
            ButtonWidget.builder(Text.translatable("minefortress.disclaimer.acknowledge_button")) {
                acknowledgeAndProceed()
            }
                .position(buttonX, buttonY)
                .size(buttonWidth, 20)
                .build()
        )
    }

    private fun acknowledgeAndProceed() {
        // 1. Mark disclaimer as acknowledged in config and save
        MineFortressClientConfig.acknowledgeDisclaimer()

        // 2. Close this screen and open the originally intended screen
        this.client?.setScreen(this.nextScreen)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context, mouseX, mouseY, delta) // Draw default background
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF) // Draw title
        super.render(context, mouseX, mouseY, delta) // Draw widgets (text, button)
    }

    override fun shouldCloseOnEsc(): Boolean {
        // Prevent closing with Esc until acknowledged
        return false
    }

    // Optional: Handle closing via other means (like Alt+F4) gracefully,
    // although forcing acknowledgment via the button is the main goal.
    // override fun close() {
    //    // Maybe log or handle differently? For now, do nothing to enforce button click.
    // }
}