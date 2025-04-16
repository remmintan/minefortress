package net.remmintan.mods.minefortress.gui.widget.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
    protected void renderItem(DrawContext drawContext) {
        if(toggledSupplier.get()) {
            final var tr = MinecraftClient.getInstance().textRenderer;
            drawContext.drawCenteredTextWithShadow(tr, "X", getX() + this.width / 2, this.getY() + this.height / 4, 0xFFFFFF);
        }
        else
            super.renderItem(drawContext);

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if(!shouldRenderSupplier.get()) return;
        super.onClick(mouseX, mouseY);
    }

    @Override
    public boolean shouldRender(boolean isCreative) {
        return super.shouldRender(isCreative) && this.shouldRenderSupplier.get();
    }

    @Override
    public boolean isHovered() {
        return super.isHovered() && shouldRenderSupplier.get();
    }
}
