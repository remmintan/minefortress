package net.remmintan.mods.minefortress.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.entity.LivingEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.remmintan.mods.minefortress.core.dtos.PawnSkin
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IPawnSkinnable
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.gui.widget.PawnSkinButton
import net.remmintan.mods.minefortress.networking.c2s.C2SSetPawnSkinPacket
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper

class FortressConfigurationScreen(private val fakePawnProvider: () -> LivingEntity) :
    Screen(Text.translatable("minefortress.config.title")) { // Use translatable title

    private var selectedSkin: PawnSkin? = null
    private var hoveredSkin: PawnSkin? = null

    private val buttonSide = 32
    private val buttonSpacing = 4
    private var confirmButton: ButtonWidget? = null

    private var startX: Int = 0
    private var startY: Int = 0
    private var entityPreviewX: Int = 0
    private var entityPreviewY: Int = 0

    private val fakePawn: LivingEntity by lazy { fakePawnProvider() }
    private val skinButtons = mutableListOf<PawnSkinButton>()

    override fun init() {
        super.init()

        val skinValues = PawnSkin.entries // Use entries for enums
        val totalButtonWidth = skinValues.size * buttonSide + (skinValues.size - 1) * buttonSpacing
        startX = (this.width - totalButtonWidth) / 2
        startY = 60 // Buttons Y position

        // Calculate positions for entity preview area
        entityPreviewX = startX + buttonSide / 2 // Align preview start roughly with first button center
        entityPreviewY = startY + buttonSide + 15 // Below buttons

        addCenteredText(this.title, 20) // Use translatable title
        addCenteredText(Text.translatable("minefortress.config.select_skin_prompt"), startY - 15)

        var currentX = startX
        this.skinButtons.clear()
        for (skin in skinValues) {
            val button = PawnSkinButton(
                currentX,
                startY,
                buttonSide,
                skin
            ) { newlySelected ->
                if (this.selectedSkin != newlySelected) {
                    this.selectedSkin = newlySelected
                }
            }

            this.skinButtons.add(button)
            this.addDrawableChild(button)
            currentX += buttonSide + buttonSpacing
        }

        confirmButton = ButtonWidget.builder(Text.translatable("gui.done")) {
            val currentSelectedSkin = this.selectedSkin
            if (currentSelectedSkin != null && currentSelectedSkin.exclusive) {

                val player = ClientModUtils.getClientPlayer()
                val isSupporter = player is IFortressPlayerEntity && player.get_SupportLevel().patron

                if (!isSupporter) {
                    this.client?.setScreen(
                        ExclusiveSkinScreen(this, currentSelectedSkin, this.fakePawnProvider)
                    )
                    return@builder
                }
            }

            selectedSkin?.let { skin ->
                FortressClientNetworkHelper.send(C2SSetPawnSkinPacket.CHANNEL, C2SSetPawnSkinPacket(skin))
                close()
            }
        }
            .position(this.width / 2 - 100, this.height - 40)
            .size(200, 20)
            .build()
        addDrawableChild(confirmButton)

        updateConfirmButtonState()
    }

    private fun updateConfirmButtonState() {
        confirmButton?.active = selectedSkin != null
        confirmButton?.tooltip = if (selectedSkin == null) {
            Tooltip.of(Text.translatable("minefortress.config.select_skin_tooltip"))
        } else {
            null
        }
    }


    override fun tick() {
        super.tick()
        updateConfirmButtonState()
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
        if (text == null) return
        // Create a TextWidget for better handling within the UI framework
        val textWidget = TextWidget(text, this.textRenderer)
        textWidget.setPosition((this.width - textWidget.width) / 2, textY) // Center based on widget width
        this.addDrawableChild(textWidget)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context, mouseX, mouseY, delta) // Render background first
        super.render(context, mouseX, mouseY, delta) // Render widgets (text, buttons)

        // Render entity preview separately, potentially overlapping widgets if needed
        renderEntityInGui(context, mouseX.toFloat(), mouseY.toFloat())
    }

    override fun shouldCloseOnEsc() = false
    override fun shouldPause() = false

    private fun renderEntityInGui(context: DrawContext, mouseX: Float, mouseY: Float) {
        val skinToRender = hoveredSkin ?: selectedSkin ?: return
        val x = startX + 5
        val y = startY + buttonSide + 15

        val pawnToRender = fakePawn

        if (pawnToRender is IPawnSkinnable) {
            pawnToRender.pawnSkin = skinToRender
        }

        val nameY = startY + buttonSide + 15
        val nameX = entityPreviewX + 55
        context.drawTextWithShadow(this.textRenderer, skinToRender.skinName, nameX, nameY, 0xFFFFFF)
        // golden color text if the skin is exclusive that it is exclusive
        if (skinToRender.exclusive) {
            context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("minefortress.config.exclusive_tag").formatted(Formatting.GOLD),
                nameX,
                nameY + 10,
                0xFFFFFF
            )
        }
        InventoryScreen.drawEntity(context, x, y, x + 50, y + 78, 45, 0.4f, mouseX, mouseY, pawnToRender)
    }
}