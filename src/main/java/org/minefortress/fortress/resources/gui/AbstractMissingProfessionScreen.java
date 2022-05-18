package org.minefortress.fortress.resources.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreen;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.professions.ProfessionsScreen;

public abstract class AbstractMissingProfessionScreen extends Screen {

    public AbstractMissingProfessionScreen() {
        super(new LiteralText("Missing Profession"));
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 2 + 24 - 16, 204, 20, new LiteralText("Back"), button -> {
            this.client.setScreen(null);
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 2 + 48 - 16, 204, 20, new LiteralText("To professions menu"), button -> {
            this.client.setScreen(new ProfessionsScreen(getClient()));
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        final var missingProfessionsText = String.format("You need at least one %s in your village", getMissingProfession());
        FortressCraftingScreen.drawCenteredText(matrices, this.textRenderer, missingProfessionsText, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        FortressCraftingScreen.drawCenteredText(matrices, this.textRenderer, "Go to professions menu and hire one", this.width / 2, this.height / 2 - 25, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @NotNull
    protected abstract String getMissingProfession();

    private FortressMinecraftClient getClient() {
        return (FortressMinecraftClient) MinecraftClient.getInstance();
    }

}
