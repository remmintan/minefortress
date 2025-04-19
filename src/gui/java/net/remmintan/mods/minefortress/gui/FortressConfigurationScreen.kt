package net.remmintan.mods.minefortress.gui

import PawnSkin
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.entity.LivingEntity
import net.minecraft.text.Text
import net.remmintan.mods.minefortress.gui.widget.PawnSkinButton

class FortressConfigurationScreen(private val fakePawnProvider: () -> LivingEntity) :
    Screen(Text.of("Let's configure your village!")) {

    private var selectedSkin: PawnSkin? = null
    private var hoveredSkin: PawnSkin? = null

    private val buttonSide = 32
    private val buttonSpacing = 4
    private var confirmButton: ButtonWidget? = null

    private var startX: Int = 0
    private var startY: Int = 0

    private val fakePawn: LivingEntity by lazy { fakePawnProvider() }
    private val skinButtons = mutableListOf<PawnSkinButton>()

    override fun init() {
        super.init()

        val skinValues = PawnSkin.entries
        val totalButtonWidth = skinValues.size * buttonSide + (skinValues.size - 1) * buttonSpacing
        startX = (this.width - totalButtonWidth) / 2 // Center the buttons horizontally
        startY = 60 // Position buttons vertically (adjust as needed)

        addCenteredText(this.getTitle(), 20)
        addCenteredText(Text.literal("Select your pawn skin:"), startY - 15)

        var currentX = startX
        this.skinButtons.clear()
        for (skin in skinValues) {
            val button = PawnSkinButton(
                currentX,
                startY,
                buttonSide,
                skin
            ) { this.selectedSkin = it }

            this.skinButtons.add(button)
            this.addDrawableChild(button)

            // Move X position for the next button
            currentX += buttonSide + buttonSpacing
        }

        confirmButton = ButtonWidget.builder(Text.translatable("gui.done")) {
            println("Configuration confirmed with skin: ${selectedSkin?.name}")
            this.client?.setScreen(null) // Close the screen
        }
            .position(this.width / 2 - 100, this.height - 40) // Position example
            .size(200, 20)
            .build()
        addDrawableChild(confirmButton)
    }

    override fun tick() {
        super.tick()
        confirmButton?.active = selectedSkin != null
        checkAndUpdateHoveredSkin()
    }

    private fun checkAndUpdateHoveredSkin() {
        for (button in skinButtons) {
            if (button.isHovered) {
                hoveredSkin = button.skin
                return
            }
        }
        hoveredSkin = null
    }

    private fun addCenteredText(text: Text?, textY: Int) {
        val textWidth = this.textRenderer.getWidth(text)
        val textHeight = this.textRenderer.fontHeight
        val textX = (this.width - textWidth) / 2

        val textWidget = TextWidget(textX, textY, textWidth, textHeight, text, this.textRenderer)
        this.addDrawableChild(textWidget)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        if (context == null) return
        renderEntityInGui(context, mouseX.toFloat(), mouseY.toFloat())
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }

    private fun renderEntityInGui(context: DrawContext, mouseX: Float, mouseY: Float) {
        val skinToRender = hoveredSkin ?: selectedSkin ?: return
        val x = startX + 5
        val y = startY + buttonSide + 15
        context.drawText(this.textRenderer, skinToRender.skinName, x + 50 + 5, y, 0xFFFFFF, false)
        InventoryScreen.drawEntity(context, x, y, x + 50, y + 78, 45, 0.4f, mouseX, mouseY, fakePawn)
    }
}