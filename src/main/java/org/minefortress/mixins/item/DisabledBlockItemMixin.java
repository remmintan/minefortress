package org.minefortress.mixins.item;

import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {
        BoneMealItem.class
})
public class DisabledBlockItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        final var player = context.getPlayer();
        if(ModUtils.isFortressGamemode(player)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

}
