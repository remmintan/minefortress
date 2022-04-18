package org.minefortress.renderer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BecomePatronScreen extends Screen {

    private static final Identifier BACKGROUND_TEXTURE_IDENTIFIER = new Identifier("textures/gui/demo_background.png");
    private static final String JOIN_LINK = "https://www.patreon.com/join/minefortress?utm_source=in_game_button&utm_medium=screen&utm_campaign=in_game_button";

    private static final int BACKGROUND_WIDTH = 248;
    private static final int BACKGROUND_HEIGHT = 166;

    private int x;
    private int y;

    private final Screen parent;
    private final String featureText;

    private static final String BECOME_A_PATRON_TEXT = "%s is only available to our Patrons. Only two cups of coffee (4€) a month and you get access to %s and get:\n- Unlimited blueprints editing\n- 'Silver' Discord rank\n- Access to Patron only Discord channels\n";

    private List<Text> preparedTextParts;

    public BecomePatronScreen(String featureText) {
        this(null, featureText);
    }

    public BecomePatronScreen(Screen parent, String featureText) {
        super(new LiteralText("Become a Patron"));
        this.parent = parent;
        this.featureText = featureText;
    }

    @Override
    protected void init() {
        super.init();

        this.x = this.width / 2 - BACKGROUND_WIDTH / 2;
        this.y = this.height / 2 - BACKGROUND_HEIGHT / 2 - 25;

        final int buttonX = this.width / 2 - 102;
        final int buttonY = this.y + BACKGROUND_HEIGHT + 5;

        final ButtonWidget becomePatron = new ButtonWidget(buttonX, buttonY, 204, 20, new LiteralText("Become a Patron"), button -> {
            Util.getOperatingSystem().open(JOIN_LINK);
            if(this.client != null) {
                this.client.setScreen(new BecomePatronConfirmationScreen(this.parent));
            }
        });
        this.addDrawableChild(becomePatron);

        final ButtonWidget backToGame = new ButtonWidget(buttonX, buttonY + 24, 204, 20, new LiteralText("Back to game"), button -> closeScreen());
        this.addDrawableChild(backToGame);

        this.preparePatronText();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        this.drawBackground(matrices);

        BecomePatronScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, this.y + 5, 0xFFFFFF);
        int i = 0;
        for (Text preparedTextPart : this.preparedTextParts) {
            BecomePatronScreen.drawTextWithShadow(matrices, this.textRenderer, preparedTextPart, this.x+5, (int)(this.y +5 + textRenderer.fontHeight +5 + i++*(1.5*textRenderer.fontHeight)), 0xFFFFFF);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        super.close();
        this.closeScreen();
    }

    private void closeScreen() {
        if(client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private void drawBackground(MatrixStack matrices) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE_IDENTIFIER);
        this.drawTexture(matrices, this.x, this.y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    private void preparePatronText() {
        this.preparedTextParts = new ArrayList<>();
        final String formattedString = String.format(BECOME_A_PATRON_TEXT, featureText, featureText.toLowerCase(Locale.ROOT));
        for (String line : formattedString.split("\n")) {
            final StringBuilder stringBuilder = new StringBuilder();
            for (String part : line.split(" ")) {
                stringBuilder.append(part).append(" ");
                final int lastPartLength = part.length();
                if (this.textRenderer.getWidth(stringBuilder.toString()) > (BACKGROUND_WIDTH - 10)) {
                    stringBuilder.delete(stringBuilder.length() - lastPartLength - 1, stringBuilder.length());
                    this.preparedTextParts.add(new LiteralText(stringBuilder.toString()));
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append(part).append(" ");
                }
            }
            this.preparedTextParts.add(new LiteralText(stringBuilder.toString()));
        }
    }
}
