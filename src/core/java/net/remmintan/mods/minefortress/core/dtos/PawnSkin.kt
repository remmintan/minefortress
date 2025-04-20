package net.remmintan.mods.minefortress.core.dtos

import net.minecraft.text.Text
import net.minecraft.util.Identifier

val PAWN_SKIN_TEXTURE = Identifier("minefortress", "textures/races_big.png")

const val PAWN_SKIN_TEXTURE_SIZE_X = 128
const val PAWN_SKIN_TEXTURE_SIZE_Y = 64
const val PAWN_SKIN_SPRITE_SIZE = 32

enum class PawnSkin(val skinName: Text, val exclusive: Boolean, gridX: Int, gridY: Int) {
    VILLAGER(Text.of("Villager"), false, 0, 0),
    STEVE(Text.of("Steve"), false, 0, 1),
    ZOMBIE_VILLAGER(Text.of("Zombie Villager"), true, 1, 0),
    ZOMBIE(Text.of("Zombie"), true, 1, 1),
    HEADLESS(Text.of("Headless"), true, 2, 0);

    val v = (gridY * PAWN_SKIN_SPRITE_SIZE).toFloat()
    val u = (gridX * PAWN_SKIN_SPRITE_SIZE).toFloat()

}