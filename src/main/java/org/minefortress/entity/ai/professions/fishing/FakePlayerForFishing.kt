package org.minefortress.entity.ai.professions.fishing

import com.mojang.authlib.GameProfile
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import org.minefortress.entity.Colonist
import java.util.*

class FakePlayerForFishing private constructor(private val pawn: LivingEntity?, gp: GameProfile) :
        PlayerEntity(pawn?.world, pawn?.blockPos, pawn?.yaw ?: 0f, gp) {

    override fun getPitch(): Float {
        return pawn?.pitch ?: 0f
    }
    override fun getYaw(): Float {
        return pawn?.yaw ?: 0f
    }

    override fun isSpectator(): Boolean = false
    override fun isCreative(): Boolean = false

    companion object {
        fun getFakePlayerForFinish(pawn: Colonist): PlayerEntity {
            if(!pawn.professionId.startsWith("fish"))
                throw IllegalArgumentException("Colonist is not a fisher")

            val gameProfile = GameProfile(UUID.randomUUID(), "random")
            return FakePlayerForFishing(pawn, gameProfile)
        }
    }

}