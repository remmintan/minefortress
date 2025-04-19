import net.minecraft.text.Text
import net.minecraft.util.Identifier


val PAWN_SKIN_TEXTURE = Identifier("minefortress", "textures/races_big.png")

const val PAWN_SKIN_TEXTURE_SIZE_X = 128
const val PAWN_SKIN_TEXTURE_SIZE_Y = 64
const val PAWN_SKIN_SPRITE_SIZE = 32

enum class PawnSkin(val skinName: Text, gridX: Int, gridY: Int) {
    VILLAGER(Text.of("Villager"), 0, 0),
    STEVE(Text.of("Steve"), 0, 1),
    ZOMBIE_VILLAGER(Text.of("Zombie Villager"), 1, 0),
    HEADLESS(Text.of("Headless"), 2, 0), // Using the top-right sprite
    ZOMBIE(Text.of("Zombie"), 1, 1);

    val v = (gridY * PAWN_SKIN_SPRITE_SIZE).toFloat()
    val u = (gridX * PAWN_SKIN_SPRITE_SIZE).toFloat()

}