package org.minefortress.renderer.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ItemToggleWidget extends ItemButtonWidget {

    protected final Supplier<Boolean> toggledSupplier;
    private final Supplier<Boolean> shouldRenderSupplier;

    public ItemToggleWidget(int anchorX, int anchorY, Item item, PressAction clickAction, Function<ItemButtonWidget, Optional<String>> optTooltip,
                            Supplier<Boolean> toggledSupplier, Supplier<Boolean> shouldRenderSupplier) {
        super(anchorX, anchorY, item, clickAction, optTooltip);
        this.toggledSupplier = toggledSupplier;
        this.shouldRenderSupplier = shouldRenderSupplier;
    }

    @Override
    protected void renderItem(MatrixStack m) {
        if(toggledSupplier.get()) {
            final var tr = MinecraftClient.getInstance().textRenderer;
            drawCenteredTextWithShadow(m, tr, "X", this.x + this.width / 2, this.y + this.height / 4, 0xFFFFFF);
        }
        else
            super.renderItem(m);

    }

    @Override
    public boolean shouldRender(boolean isCreative) {
        return super.shouldRender(isCreative) && this.shouldRenderSupplier.get();
    }
}
