package org.minefortress.mixins.renderer.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.minefortress.utils.GuiUtils;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(HandledScreen.class)
public abstract class FortressHandledScreenMixin extends Screen {
    protected FortressHandledScreenMixin(Text title) {
        super(title);
    }

    // redirect draw slot to draw slot with a different signature
    @Redirect(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"))
    void renderSlotText(ItemRenderer instance, TextRenderer renderer, ItemStack stack, int x, int y, String countLabel) {
        if(ModUtils.isClientInFortressGamemode() && Objects.isNull(countLabel) && stack.getCount() > 99) {
            final var newCountLabel = GuiUtils.formatSlotCount(stack.getCount());
            instance.renderGuiItemOverlay(renderer, stack, x, y, newCountLabel);
        } else {
            instance.renderGuiItemOverlay(renderer, stack, x, y, countLabel);
        }
    }

}
