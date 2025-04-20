package net.remmintan.mods.minefortress.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.MultilineTextWidget
import net.minecraft.entity.LivingEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util
import net.remmintan.mods.minefortress.core.dtos.PAWN_SKIN_TEXTURE
import net.remmintan.mods.minefortress.core.dtos.PAWN_SKIN_TEXTURE_SIZE_X
import net.remmintan.mods.minefortress.core.dtos.PAWN_SKIN_TEXTURE_SIZE_Y
import net.remmintan.mods.minefortress.core.dtos.PawnSkin
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IPawnSkinnable
import java.net.URI
import kotlin.math.max

class ExclusiveSkinScreen(
    private val parent: Screen,
    private val lockedSkin: PawnSkin,
    private val fakePawnProvider: () -> LivingEntity
) : Screen(Text.translatable("minefortress.exclusive_skin.title")) {

    private val patreonUrl = "https://www.patreon.com/minefortress"
    private val iconSize = 32

    private val entityPreviewWidth = 50
    private val entityPreviewHeight = 50 // Height used for positioning logic

    private val entityRenderSizeParam = 45
    private val entityZOffsetParam = 0.7f

    private val contentStartY = 40
    private val textWidth = 300
    private val previewAreaYOffset = 15 // Increased offset
    private val iconTextSpacing = 4
    private val entityPreviewYOffset = 10 // Increased offset
    private val previewButtonSpacing = 15 // Horizontal space between preview and buttons
    private val buttonSpacing = 8 // Vertical space between buttons
    private val buttonWidth = 120 // Adjusted button width slightly

    private val fakePawn: LivingEntity by lazy { fakePawnProvider() }

    // Store calculated positions to use in render
    private var textWidgetYEnd = 0
    private var previewAreaXStart = 0
    private var previewAreaXEnd = 0
    private var previewAreaYStart = 0
    private var previewAreaYEnd = 0


    override fun init() {
        super.init()

        val textBlockX = (this.width - textWidth) / 2
        var currentY = contentStartY

        // --- Add Informational Text ---
        val message = Text.translatable(
            "minefortress.exclusive_skin.message",
            lockedSkin.skinName.copy().formatted(Formatting.GOLD)
        ) // Add formatting here
            .append("\n\n")
            .append(Text.translatable("minefortress.exclusive_skin.explanation"))
            .append("\n\n")
            .append(Text.translatable("minefortress.exclusive_skin.cta"))

        val textWidget = MultilineTextWidget(textBlockX, currentY, message, this.textRenderer)
            .setMaxWidth(textWidth)
            .setCentered(true)
        this.addDrawableChild(textWidget)

        // Calculate Y position below the text widget dynamically
        textWidgetYEnd = textWidget.y + textWidget.height // Store for render use
        previewAreaYStart = textWidgetYEnd + previewAreaYOffset // Y where icon/name starts

        // --- Calculate Preview Area Horizontal Bounds (used for button positioning) ---
        // We need to estimate the width of the icon/name part and the entity part
        // and take the maximum to determine the right edge for positioning buttons.
        val skinNameWidth = textRenderer.getWidth(lockedSkin.skinName)
        val iconNameTotalWidth = iconSize + iconTextSpacing + skinNameWidth
        val entityTopY = previewAreaYStart + iconSize + entityPreviewYOffset // Y where entity starts

        // Determine the effective width centered around the screen middle
        val previewContentWidth = max(iconNameTotalWidth, entityPreviewWidth)
        previewAreaXStart = (this.width - previewContentWidth - previewButtonSpacing - buttonWidth) / 2
        previewAreaXEnd = previewAreaXStart + previewContentWidth

        // Determine the bottom edge of the preview area
        previewAreaYEnd = entityTopY + entityPreviewHeight


        // --- Add Buttons (Positioned to the right of the preview area) ---
        val buttonStartX = previewAreaXEnd + previewButtonSpacing
        // Vertically center the buttons roughly within the preview area's height
        val previewTotalHeight = previewAreaYEnd - previewAreaYStart
        val totalButtonHeight = 20 + buttonSpacing + 20 // Height of two buttons + spacing
        val buttonStartY = previewAreaYStart + (previewTotalHeight - totalButtonHeight) / 2

        addDrawableChild(
            ButtonWidget.builder(Text.translatable("minefortress.exclusive_skin.unlock_button")) {
                Util.getOperatingSystem().open(URI(patreonUrl))
            }
                .position(buttonStartX, buttonStartY) // Positioned right of preview, vertically centered
                .size(buttonWidth, 20)
                .build()
        )

        addDrawableChild(
            ButtonWidget.builder(Text.translatable("gui.cancel")) { // Using CommonTexts.CANCEL is standard
                this.client?.setScreen(this.parent)
            }
                .position(buttonStartX, buttonStartY + 20 + buttonSpacing) // Below the first button
                .size(buttonWidth, 20)
                .build()
        )
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta) // Draw widgets (text, buttons)

        // --- Render Skin Preview Elements --- (Using calculated positions from init)
        val textHeight = textRenderer.fontHeight

        // Calculate horizontal positions for icon/name based on stored preview start X
        val skinNameWidth = textRenderer.getWidth(lockedSkin.skinName)
        val iconNameTotalWidth = iconSize + iconTextSpacing + skinNameWidth
        // Center icon/name combo within the preview area width
        val iconX = previewAreaXStart + ((previewAreaXEnd - previewAreaXStart) - iconNameTotalWidth) / 2
        val nameX = iconX + iconSize + iconTextSpacing
        // Use stored previewAreaYStart for the top Y
        val iconNameY = previewAreaYStart + (iconSize - textHeight) / 2 // Vertically center name with icon

        // Draw Icon
        context.drawTexture(
            PAWN_SKIN_TEXTURE,
            iconX, previewAreaYStart, // Use stored Y start
            lockedSkin.u.toFloat(), lockedSkin.v.toFloat(),
            iconSize, iconSize,
            PAWN_SKIN_TEXTURE_SIZE_X, PAWN_SKIN_TEXTURE_SIZE_Y
        )

        // Draw Skin Name (with gold color)
        context.drawTextWithShadow(
            this.textRenderer,
            lockedSkin.skinName.copy().formatted(Formatting.GOLD),
            nameX, iconNameY,
            0xFFFFFF
        )

        // Calculate position for the 3D entity preview
        val entityTopY = previewAreaYStart + iconSize + entityPreviewYOffset // Below icon/name area
        // Center the entity horizontally within the preview X bounds
        val entityCenterX = previewAreaXStart + (previewAreaXEnd - previewAreaXStart) / 2

        // --- Render Entity using the specific 10-argument drawEntity ---
        renderEntityPreview(context, entityCenterX, entityTopY, mouseX, mouseY)
    }

    /**
     * Renders the entity preview using the 10-argument drawEntity call,
     * with mouse coordinates for tracking.
     */
    private fun renderEntityPreview(context: DrawContext, centerX: Int, topY: Int, mouseX: Int, mouseY: Int) {
        val skinToRender = this.lockedSkin
        val pawnToRender = fakePawn

        (pawnToRender as? IPawnSkinnable)?.pawnSkin = skinToRender

        // Define the rendering rectangle based on center and implicit size
        val x1 = centerX - entityPreviewWidth / 2
        val y1 = topY
        val x2 = x1 + entityPreviewWidth
        val y2 = y1 + entityPreviewHeight // Using the defined height for the render box

        // Call the specific drawEntity method with the exact parameters
        InventoryScreen.drawEntity(
            context,
            x1,                       // x1
            y1,                       // y1
            x2,                       // x2
            y2,                       // y2
            entityRenderSizeParam,    // size parameter (45)
            entityZOffsetParam,       // zOffset parameter (0.4f)
            mouseX.toFloat(),         // Pass actual mouseX for tracking
            mouseY.toFloat(),         // Pass actual mouseY for tracking
            pawnToRender              // entity
        )
    }

    override fun shouldCloseOnEsc(): Boolean {
        return true
    }

    override fun close() {
        this.client?.setScreen(this.parent)
    }
}