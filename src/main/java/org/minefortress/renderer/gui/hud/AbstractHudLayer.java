package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.minefortress.utils.ModUtils;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractHudLayer extends DrawableHelper {

    private final List<IHudButton> fortressHudButtons = new ArrayList<>();

    protected final MinecraftClient client;
    protected final ItemRenderer itemRenderer;
    protected final TextRenderer textRenderer;

    private int basepointX;
    private int basepointY;
    private boolean centeredX;
    private boolean centeredY;

    protected AbstractHudLayer(MinecraftClient client, ItemRenderer itemRenderer) {
        this.client = client;
        this.itemRenderer = itemRenderer;
        this.textRenderer = client.textRenderer;
    }

    protected final void setBasepoint(int x, int y, boolean centeredX, boolean centeredY) {
        this.basepointX = x;
        this.basepointY = y;
        this.centeredX = centeredX;
        this.centeredY = centeredY;
    }

    protected final void addButton(IHudButton button) {
        if(button instanceof IItemButton itemButton)
            itemButton.setItemRenderer(itemRenderer);
        fortressHudButtons.add(button);
    }

    void tick() {

    }
    final void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        final var baseX = centeredX ? screenWidth / 2 : screenWidth + basepointX;
        final var baseY = centeredY ? screenHeight / 2 : screenHeight + basepointY;

        boolean creative = ModUtils.getFortressClientManager().isCreative();
        for (IHudButton fortressHudButton : fortressHudButtons) {
            fortressHudButton.setPos(baseX, baseY);
            if(fortressHudButton.shouldRender(creative)) {
                fortressHudButton.render(p, (int)mouseX, (int)mouseY, delta);
            }
        }
    }
    abstract boolean isHovered();
    void onClick(double mouseX, double mouseY) {}
}
