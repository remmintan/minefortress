package org.minefortress.mixins.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {
        BowItem.class,
        EggItem.class,
        BoatItem.class,
        SnowballItem.class,
        ExperienceBottleItem.class,
        FishingRodItem.class,
        EnderPearlItem.class,
        EnderEyeItem.class,
        TridentItem.class,
        PotionItem.class,
        SplashPotionItem.class,
        LingeringPotionItem.class
})
public class DisableItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (FortressGamemodeUtilsKt.isFortressGamemode(user)) {
            cir.setReturnValue(TypedActionResult.pass(user.getStackInHand(hand)));
        }
    }

}
