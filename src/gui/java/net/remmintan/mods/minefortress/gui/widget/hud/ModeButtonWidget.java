package net.remmintan.mods.minefortress.gui.widget.hud;

import net.minecraft.item.Item;

import java.util.Optional;
import java.util.function.Supplier;

public class ModeButtonWidget extends ItemButtonWidget {
    private final Supplier<Boolean> isActiveSupplier;

    public ModeButtonWidget(
            int anchorX, int anchorY, Item item, PressAction clickAction, String tooltipText, Supplier<Boolean> isActiveSupplier
    ) {
        super(anchorX, anchorY, item, clickAction, tooltipText);
        this.isActiveSupplier = isActiveSupplier;
    }

    public ModeButtonWidget(
            int anchorX, int anchorY, Item item, PressAction clickAction, Supplier<String> tooltipTextSupplier, Supplier<Boolean> isActiveSupplier
    ) {
        super(anchorX, anchorY, item, clickAction, itemButtonWidget -> Optional.of(tooltipTextSupplier.get()));
        this.isActiveSupplier = isActiveSupplier;
    }

    @Override
    public boolean isFocused() {
        return this.isActiveSupplier.get();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if(!active) return;
        super.onClick(mouseX, mouseY);
    }
}
