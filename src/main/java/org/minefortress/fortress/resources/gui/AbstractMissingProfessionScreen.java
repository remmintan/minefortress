package org.minefortress.fortress.resources.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralTextContent;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreen;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.professions.ProfessionsScreen;

public abstract class AbstractMissingProfessionScreen extends Screen {

    protected final boolean irregularReson;

    public AbstractMissingProfessionScreen() {
        this(false);
    }

    public AbstractMissingProfessionScreen(boolean irregularReson) {
        super(new LiteralTextContent("Missing Profession"));
        this.irregularReson = irregularReson;
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 2 + 24 - 16, 204, 20, new LiteralTextContent("Back"), button -> {
            if(this.client != null)
                this.client.setScreen(null);
        }));

        if(!irregularReson) {
            this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 2 + 48 - 16, 204, 20, new LiteralTextContent("To professions menu"), button -> {
                if(this.client != null)
                    this.client.setScreen(new ProfessionsScreen(getClient()));
            }));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        final var missingText = String.format("You need at least one %s in your village", getMissingObjectName());
        FortressCraftingScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, missingText, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        if(irregularReson) {
            FortressCraftingScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, getActionText(), this.width / 2, this.height / 2 - 25, 0xFFFFFF);
        } else {
            FortressCraftingScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, "Go to professions menu and hire one", this.width / 2, this.height / 2 - 25, 0xFFFFFF);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @NotNull
    protected abstract String getMissingObjectName();

    protected String getActionText() {
        throw new NotImplementedException("This method should be implemented in child class");
    }

    private FortressMinecraftClient getClient() {
        return (FortressMinecraftClient) MinecraftClient.getInstance();
    }

}
