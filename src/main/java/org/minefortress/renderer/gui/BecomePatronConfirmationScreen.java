package org.minefortress.renderer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class BecomePatronConfirmationScreen extends Screen {

    private static final Identifier BACKGROUND_TEXTURE_IDENTIFIER = new Identifier("textures/gui/demo_background.png");

    private static final int BACKGROUND_WIDTH = 195;
    private static final int BACKGROUND_HEIGHT = 136;

    private final Screen parent;

    private int x;
    private int y;

    public BecomePatronConfirmationScreen(Screen parent) {
        super(new LiteralText("Thank you for your support!"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.x = this.width / 2 - BACKGROUND_WIDTH / 2;
        this.y = this.height / 2 - BACKGROUND_HEIGHT / 2;

        final int buttonX = this.width / 2 - 102;
        final int buttonY = this.y + BACKGROUND_HEIGHT + 5;

        final ButtonWidget backToGame = new ButtonWidget(buttonX, buttonY, 204, 20, new LiteralText("Back to game"), button -> closeScreen());
        this.addDrawableChild(backToGame);
    }

    private void drawBackground(MatrixStack matrices) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE_IDENTIFIER);
        this.drawTexture(matrices, this.x, this.y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        this.drawBackground(matrices);

        BecomePatronScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, this.y + 5, 0xFFFFFF);
        BecomePatronScreen.drawCenteredText(
                matrices,
                this.textRenderer,
                new LiteralText("Browser window will open in a few seconds!"),
                this.width / 2,
                this.y + 20 ,
                0xFFFFFF
        );
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.closeScreen();
    }

    private void closeScreen() {
        if(client != null) {
            this.client.setScreen(this.parent);
        }
    }

}
