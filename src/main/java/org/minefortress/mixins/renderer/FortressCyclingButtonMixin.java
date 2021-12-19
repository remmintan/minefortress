package org.minefortress.mixins.renderer;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(CyclingButtonWidget.class)
public abstract class FortressCyclingButtonMixin {

    @Shadow private CyclingButtonWidget.Values values;

    @Shadow @Final private Function<CyclingButtonWidget<CreateWorldScreen.Mode>, MutableText> narrationMessageFactory;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(int x, int y, int width, int height, Text message, Text optionText, int index, Object value, CyclingButtonWidget.Values values, Function valueToText, Function narrationMessageFactory, CyclingButtonWidget.UpdateCallback callback, CyclingButtonWidget.TooltipFactory tooltipFactory, boolean optionTextOmitted, CallbackInfo ci) {
        if(value instanceof CreateWorldScreen.Mode) {
            List newValues = new ArrayList();
            values.getCurrent().forEach(newValues::add);
            CreateWorldScreen.Mode.DEBUG.defaultGameMode = ClassTinkerers.getEnum(GameMode.class, "FORTRESS");
            CreateWorldScreen.Mode.DEBUG.translationSuffix = "fortress";
            CreateWorldScreen.Mode.DEBUG.text = new TranslatableText("selectWorld.gameMode." + "fortress");
            newValues.add(CreateWorldScreen.Mode.DEBUG);

            this.values = CyclingButtonWidget.Values.of(newValues);
        }
    }

}
