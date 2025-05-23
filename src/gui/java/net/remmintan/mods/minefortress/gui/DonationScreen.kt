package net.remmintan.mods.minefortress.gui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import java.net.URI
import kotlin.math.min

private val BACKGROUND_TEXTURE = Identifier("minefortress", "textures/gui/donation_background.png")
private const val DONATION_URL = "https://www.minefortress.net/donate" // Example URL

class DonationScreen : Screen(Text.translatable("minefortress.donation.title")) {

    //    private val subtitleText = Text.translatable("minefortress.donation.subtitle")
    private val supportButtonText = Text.translatable("minefortress.donation.support_button")
    private val declineButtonText = Text.translatable("minefortress.donation.decline_button")

    private val contentPadding = 10
    private val buttonHeight = 20
    private val buttonWidth = 150
    private val buttonSpacing = 10

    private val donateImage = DonateImage()

    override fun init() {
        super.init()

        val bottomButtonY = this.height - contentPadding - buttonHeight
        val totalButtonWidth = buttonWidth * 2 + buttonSpacing
        val buttonsStartX = (this.width - totalButtonWidth) / 2

        resizeDonateImage()
        addDrawableChild(donateImage)

        // Support Button
        addDrawableChild(
            ButtonWidget.builder(supportButtonText) {
                Util.getOperatingSystem().open(URI(DONATION_URL))
                // Optionally, you could close the screen or show a thank you message here too
            }
                .dimensions(buttonsStartX, bottomButtonY, buttonWidth, buttonHeight)
                .build()
        )

        // Decline Button
        addDrawableChild(
            ButtonWidget.builder(declineButtonText) { this.close() }
                .dimensions(buttonsStartX + buttonWidth + buttonSpacing, bottomButtonY, buttonWidth, buttonHeight)
                .build()
        )
    }

    override fun resize(client: MinecraftClient?, width: Int, height: Int) {
        super.resize(client, width, height)
        resizeDonateImage()
    }

    private fun resizeDonateImage() {
        val imageOriginalWidth = 1024
        val imageOriginalHeight = 1536

        val scale = min(this.width.toFloat() / imageOriginalWidth, this.height.toFloat() / imageOriginalHeight) * 0.9f
        val drawWidth = (imageOriginalWidth * scale).toInt()
        val drawHeight = (imageOriginalHeight * scale).toInt()
        donateImage.drawX = (this.width - drawWidth) / 2
        donateImage.drawY = (this.height - drawHeight) / 2
        donateImage.drawWidth = drawWidth
        donateImage.drawHeight = drawHeight
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        // Calculate scaled image dimensions to fit the screen while maintaining aspect ratio


        // Draw the background image


//        // Draw title centered at the top
//        context.drawCenteredTextWithShadow(
//            this.textRenderer,
//            this.title,
//            this.width / 2,
//            contentPadding,
//            0xFFFFFF
//        )

        // Draw subtitle below the title
//        val subtitleWidget = MultilineTextWidget(
//            (this.width - (this.width - contentPadding*2)) / 2, // Center the text block
//            contentPadding + this.textRenderer.fontHeight + 5, // Below title
//            subtitleText,
//            this.textRenderer
//        )
//        subtitleWidget.setMaxWidth(this.width - contentPadding * 2) // Allow text to wrap
//        subtitleWidget.setCentered(true) // Center the text lines
//        subtitleWidget.render(context, mouseX, mouseY, delta)

    }

    override fun shouldCloseOnEsc(): Boolean {
        // Prevent closing with Esc to encourage making a choice via buttons
        return false
    }

    override fun close() {
        this.client?.setScreen(null)
    }

    override fun shouldPause(): Boolean = true // Pause the game when this screen is open

    private inner class DonateImage : Drawable, Selectable, Element {

        var drawX = 0
        var drawY = 0
        var drawWidth = 0
        var drawHeight = 0

        override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
            context ?: return
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f) // Ensure full opacity for the image
            context.drawTexture(BACKGROUND_TEXTURE, drawX, drawY, 0f, 0f, drawWidth, drawHeight, drawWidth, drawHeight)

        }

        override fun appendNarrations(builder: NarrationMessageBuilder?) {}
        override fun getType() = Selectable.SelectionType.NONE
        override fun setFocused(focused: Boolean) {}
        override fun isFocused() = false
    }
}

