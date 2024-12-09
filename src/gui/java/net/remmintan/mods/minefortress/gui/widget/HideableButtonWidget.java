package net.remmintan.mods.minefortress.gui.widget;

import java.util.function.Supplier;

public class HideableButtonWidget extends TextButtonWidget {

    private final Supplier<Boolean> shouldRender;

    public HideableButtonWidget(int anchorX,
                                int anchorY,
                                int width,
                                int height,
                                String message,
                                PressAction onPress,
                                String tooltipText,
                                Supplier<Boolean> shouldRender
    ) {
        super(anchorX, anchorY, width, height, message, onPress, tooltipText);
        this.shouldRender = shouldRender;
    }

    @Override
    public boolean shouldRender(boolean isCreative) {
        return shouldRender.get();
    }

    @Override
    public boolean isHovered() {
        return super.isHovered();
    }
}
