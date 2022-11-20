package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.minefortress.renderer.gui.hud.interfaces.IHudButton;
import org.minefortress.renderer.gui.hud.interfaces.IHudElement;
import org.minefortress.renderer.gui.hud.interfaces.IItemHudElement;
import org.minefortress.utils.ModUtils;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractHudLayer extends DrawableHelper {

    private final List<IHudElement> fortressHudElements = new ArrayList<>();

    protected final MinecraftClient client;
    protected final ItemRenderer itemRenderer;
    protected final TextRenderer textRenderer;

    private Integer basepointX;
    private Integer basepointY;
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

    protected final void addElement(IHudElement button) {
        if(button instanceof IItemHudElement itemButton)
            itemButton.setItemRenderer(itemRenderer);
        fortressHudElements.add(button);
    }

    void tick() {
        for(IHudElement button : fortressHudElements)
            button.tick();
    }

    final void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        if(basepointX == null || basepointY == null) throw new IllegalStateException("Basepoint not set!");

        final var baseX = centeredX ? screenWidth / 2 : screenWidth + basepointX;
        final var baseY = centeredY ? screenHeight / 2 : screenHeight + basepointY;

        boolean creative = ModUtils.getFortressClientManager().isCreative();
        for (IHudElement fortressHudButton : fortressHudElements) {
            fortressHudButton.setPosBasedOn(baseX, baseY);
            if(fortressHudButton.shouldRender(creative)) {
                fortressHudButton.render(p, (int)mouseX, (int)mouseY, delta);
            }
        }
    }
    final boolean isHovered() {
        for (IHudElement fortressHudButton : fortressHudElements) {
            if(fortressHudButton.isHovered()) return true;
        }
        return false;
    }
    final void onClick(double mouseX, double mouseY) {
        for (IHudElement elem : fortressHudElements) {
            if(elem instanceof IHudButton btn && elem.isHovered()) {
                btn.onClick(mouseX, mouseY);
                return;
            }
        }
    }
}
