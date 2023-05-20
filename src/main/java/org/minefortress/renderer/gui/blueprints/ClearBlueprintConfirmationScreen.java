package org.minefortress.renderer.gui.blueprints;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.minefortress.network.c2s.C2SClearActiveBlueprint;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

@Environment(value= EnvType.CLIENT)
public class ClearBlueprintConfirmationScreen extends Screen {

    private final static Text TITLE = Text.of("Confirm Clearing Blueprint");
    private final static Text CONFIRMATION_TEXT = Text.of("Do you really want to clear the blueprint?");

    private final Screen parent;

    public ClearBlueprintConfirmationScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 24 - 16, 204, 20, new LiteralText("Yes"), button -> {
            sendClear();
            if(super.client != null) {
                super.client.setScreen(null);
            }
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 48 - 16, 204, 20, new LiteralText("No"), button -> close()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        GameMenuScreen.drawCenteredText(matrices, this.textRenderer, CONFIRMATION_TEXT, this.width / 2, 40, 0xFFFFFF);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    private void sendClear() {
        var packet = new C2SClearActiveBlueprint();
        FortressClientNetworkHelper.send(C2SClearActiveBlueprint.CHANNEL, packet);
    }

    @Override
    public void close() {
        if(super.client != null) {
            super.client.setScreen(this.parent);
        }
    }
}
