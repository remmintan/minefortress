package org.minefortress.renderer.gui.widget;

import java.util.function.Supplier;

public class FortressDynamicVisibilityButtonWidget extends FortressTextButtonWidget {

    private final Supplier<Boolean> shouldRender;

    public FortressDynamicVisibilityButtonWidget(int anchorX,
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
}
