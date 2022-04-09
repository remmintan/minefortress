package org.minefortress.renderer.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class TimeButtonWidget extends ButtonWidget {

    private static final Identifier SELECTOR_TEXTURE = new Identifier("textures/gui/server_selection.png");
    private static final int ARROW_SIZE = 16;
    private static final int ARROW_PADDING = 4;
    private static final int ARROW_U = 16;
    private static final int ARROW_V = 112;

    private final Supplier<Boolean> active;

    public TimeButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, Supplier<Boolean> active) {
        super(x, y, width, height, message, onPress);
        this.active = active;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        if(active.get()){
            int arrowY = this.y + this.height + ARROW_PADDING;
            int arrowX = this.x + this.width / 2 - ARROW_SIZE / 2;

            RenderSystem.setShaderTexture(0, SELECTOR_TEXTURE);
            super.drawTexture(matrices, arrowX, arrowY, ARROW_U, ARROW_V, ARROW_SIZE, ARROW_SIZE);
        }
    }
}
