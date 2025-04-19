package net.remmintan.mods.minefortress.gui.widget

import PAWN_SKIN_SPRITE_SIZE
import PAWN_SKIN_TEXTURE
import PAWN_SKIN_TEXTURE_SIZE_X
import PAWN_SKIN_TEXTURE_SIZE_Y
import PawnSkin
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.util.Identifier

val BUTTON_TEXTURE = Identifier("minefortress", "textures/gui/btn_big.png")

class PawnSkinButton(
    x: Int,
    y: Int,
    side: Int,
    val skin: PawnSkin,
    private val onPress: (PawnSkin) -> Unit

) : ClickableWidget(x, y, side, side, skin.skinName) {

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val vOffset = if (this.isHovered || this.isFocused) 32 else 0

        // Draw button background
        context.drawTexture(
            BUTTON_TEXTURE,
            this.x,
            this.y,
            0f, // U coordinate in button texture
            vOffset.toFloat(), // V coordinate in button texture
            this.width, // Width to draw
            this.height, // Height to draw
            32, // Width of the texture region
            64 // Total height of the button texture file (assuming two states stacked)
        )

        // Calculate position to center the skin sprite inside the button
        val spriteDrawX = this.x + (this.width - PAWN_SKIN_SPRITE_SIZE) / 2
        val spriteDrawY = this.y + (this.height - PAWN_SKIN_SPRITE_SIZE) / 2

        // Draw the pawn skin sprite
        context.drawTexture(
            PAWN_SKIN_TEXTURE,
            spriteDrawX,
            spriteDrawY,
            skin.u, // U coordinate on skin texture
            skin.v, // V coordinate on skin texture
            PAWN_SKIN_SPRITE_SIZE, // Width of sprite to draw
            PAWN_SKIN_SPRITE_SIZE, // Height of sprite to draw
            PAWN_SKIN_TEXTURE_SIZE_X, // Total width of the skin texture file
            PAWN_SKIN_TEXTURE_SIZE_Y  // Total height of the skin texture file
        )

    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {}

    override fun onRelease(mouseX: Double, mouseY: Double) {
        onPress(skin)
    }


}