package org.minefortress.renderer.gui.widget;

import net.minecraft.item.Item;

import java.util.function.Supplier;

public class ModeButtonWidget extends ItemButtonWidget{

    private final Supplier<Boolean> isActiveSupplier;

    public ModeButtonWidget(
            int anchorX, int anchorY, Item item, PressAction clickAction, String tooltipText, Supplier<Boolean> isActiveSupplier
    ) {
        super(anchorX, anchorY, item, clickAction, tooltipText);
        this.isActiveSupplier = isActiveSupplier;
    }

    @Override
    public void tick() {
        super.tick();
        this.active = isActiveSupplier.get();
    }
}
