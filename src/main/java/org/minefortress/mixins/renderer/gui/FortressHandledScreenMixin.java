package org.minefortress.mixins.renderer.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import net.remmintan.mods.minefortress.gui.util.GuiUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(HandledScreen.class)
public abstract class FortressHandledScreenMixin extends Screen {
    protected FortressHandledScreenMixin(Text title) {
        super(title);
    }

    @Redirect(method = "drawSlot", at  = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"))
    void changeSlotText(DrawContext instance, TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride) {
        if (FortressGamemodeUtilsKt.isClientInFortressGamemode() && Objects.isNull(countOverride) && stack.getCount() > 99) {
            final var newCountLabel = GuiUtils.formatSlotCount(stack.getCount());
            instance.drawItemInSlot(textRenderer, stack, x, y, newCountLabel);
        } else {
            instance.drawItemInSlot(textRenderer, stack, x, y, countOverride);
        }
    }

}
