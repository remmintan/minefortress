package org.minefortress.mixins.renderer.gui;

import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.minefortress.MineFortressMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.function.Function;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(CyclingButtonWidget.class)
public abstract class FortressCyclingButtonMixin<T> extends PressableWidget {

    @Shadow private CyclingButtonWidget.Values<T> values;

    @Shadow private Object value;

    @Shadow private int index;

    @Shadow protected abstract Text composeText(Object value);

    public FortressCyclingButtonMixin(int i, int j, int k, int l, Text text) {
        super(i, j, k, l, text);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(int x,
                     int y,
                     int width,
                     int height,
                     Text message,
                     Text optionText,
                     int index,
                     Object value,
                     CyclingButtonWidget.Values values,
                     Function valueToText,
                     Function narrationMessageFactory,
                     CyclingButtonWidget.UpdateCallback callback,
                     SimpleOption.TooltipFactory tooltipFactory,
                     boolean optionTextOmitted,
                     CallbackInfo ci) {
        if(value instanceof WorldCreator.Mode) {
            var newValues = new ArrayList(values.getCurrent());
            WorldCreator.Mode.DEBUG.defaultGameMode = MineFortressMod.FORTRESS;
            WorldCreator.Mode.DEBUG.name = Text.translatable("selectWorld.gameMode.fortress");
            WorldCreator.Mode.DEBUG.info =  Text.translatable("selectWorld.gameMode.fortress");
            newValues.add(WorldCreator.Mode.DEBUG);

            this.values = CyclingButtonWidget.Values.of(newValues);
            this.value = WorldCreator.Mode.DEBUG;
            this.index = 3;
            this.setMessage(this.composeText(this.value));
        }
    }
}
