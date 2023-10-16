package org.minefortress.renderer.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import org.minefortress.utils.ModUtils;

import java.util.Objects;

public class ChooseModeScreen extends Screen {

    private final Text questionText = Text.literal("Choose a gamemode for this world");
    private final Text loadingText = Text.literal("Loading...");
    private boolean loading = false;

    public  ChooseModeScreen() {
        super(Text.literal("Choose Game Mode"));
    }

    @Override
    protected void init() {
        final var creativeBtn = ButtonWidget
                .builder(Text.literal("Creative"), button -> {
                    setLoading();
                    ModUtils.getFortressClientManager().setGamemode(FortressGamemode.CREATIVE);
                })
                .dimensions(this.width / 2 - 102, this.height / 4 + 24 - 16, 204, 20)
                .build();
        this.addDrawableChild(creativeBtn);

        final var survivalBtn = ButtonWidget
                .builder(Text.literal("Survival"), button -> {
                    setLoading();
                    ModUtils.getFortressClientManager().setGamemode(FortressGamemode.SURVIVAL);
                })
                .dimensions(this.width / 2 - 102, this.height / 4 + 48 - 16, 204, 20)
                .build();
        this.addDrawableChild(survivalBtn);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if(!ModUtils.getFortressClientManager().gamemodeNeedsInitialization()) Objects.requireNonNull(this.client).setScreen(null);
        super.renderBackground(drawContext);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, loading?loadingText:questionText, this.width / 2, 40, 0xFFFFFF);
        if(loading) return;
        super.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void setLoading() {
        this.loading = true;
    }
}
