package org.minefortress.renderer.gui.widget;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import org.minefortress.utils.ModUtils;

public class FreePawnsWidget extends MinefortressWidget implements Drawable, Element {

    // get x, y as input render free pawns at that x, y
    private final int x;
    private final int y;

    public FreePawnsWidget(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        final var freePawns = getFreePawns();
        drawContext.drawTextWithShadow(
                this.getTextRenderer(),
                "Free Pawns: " + freePawns,
                x,
                y,
                freePawns > 0 ? 0xFFFFFF : 0xFF0000
        );
    }

    private int getFreePawns() {
        return ModUtils.getProfessionManager().getFreeColonists();
    }
}
