package net.remmintan.mods.minefortress.gui.widget.professions;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class ProgressArrowWidget implements Drawable, Element {

    private static final Identifier PROGRESS_TEXTURE = new Identifier("textures/gui/container/blast_furnace.png");
    private static final Identifier BURN_PROGRESS_TEXTURE = new Identifier("container/furnace/burn_progress");

    private final Supplier<Integer> progressSupplier;
    private final int x;
    private final int y;

    public ProgressArrowWidget(int x, int y, Supplier<Integer> progressSupplier) {
        this.x = x;
        this.y = y;
        this.progressSupplier = progressSupplier;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.drawTexture(PROGRESS_TEXTURE, x-11, y+2, 80, 35, 22, 15, 256, 256);
        int rawProgress = progressSupplier.get();
        int progress = (int) (rawProgress * 0.22);
        if (progress > 0) {
            drawContext.drawGuiTexture(BURN_PROGRESS_TEXTURE, 24, 16, 0, 0, x-11, y+2, progress+2, 16);
        }
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
