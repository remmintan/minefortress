package org.minefortress.renderer.gui.blueprints;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.networking.c2s.C2SClearActiveBlueprint;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;

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

        final var yesBtn = ButtonWidget
                .builder(Text.literal("Yes"), button -> {
                    sendClear();
                    if (super.client != null) {
                        super.client.setScreen(null);
                    }
                })
                .dimensions(this.width / 2 - 102, this.height / 4 + 24 - 16, 204, 20)
                .build();
        this.addDrawableChild(yesBtn);

        final var noBtn = ButtonWidget
                .builder(Text.literal("No"), button -> close())
                .dimensions(this.width / 2 - 102, this.height / 4 + 48 - 16, 204, 20)
                .build();
        this.addDrawableChild(noBtn);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.renderBackground(drawContext);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, CONFIRMATION_TEXT, this.width / 2, 40, 0xFFFFFF);

        super.render(drawContext, mouseX, mouseY, delta);
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
