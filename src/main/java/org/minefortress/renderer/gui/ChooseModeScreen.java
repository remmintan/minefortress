package org.minefortress.renderer.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;

import java.util.Objects;

public class ChooseModeScreen extends Screen {

    private final Text questionText = Text.translatable("key.minefortress.choose_mode_screen.question_text");
    private final Text loadingText = Text.translatable("key.minefortress.choose_mode_screen.loading_text");
    private boolean loading = false;

    public  ChooseModeScreen() {
        super(Text.translatable("key.minefortress.choose_mode_screen"));
    }

    @Override
    protected void init() {
        final var creativeBtn = ButtonWidget
                .builder(Text.translatable("key.minefortress.choose_mode_screen.creative_btn"), button -> {
                    setLoading();
                    CoreModUtils.getFortressManager().setGamemode(FortressGamemode.CREATIVE);
                })
                .dimensions(this.width / 2 - 102, this.height / 4 + 24 - 16, 204, 20)
                .build();
        this.addDrawableChild(creativeBtn);

        final var survivalBtn = ButtonWidget
                .builder(Text.translatable("key.minefortress.choose_mode_screen.survival_btn"), button -> {
                    setLoading();
                    CoreModUtils.getFortressManager().setGamemode(FortressGamemode.SURVIVAL);
                })
                .dimensions(this.width / 2 - 102, this.height / 4 + 48 - 16, 204, 20)
                .build();
        this.addDrawableChild(survivalBtn);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (!CoreModUtils.getFortressManager().gamemodeNeedsInitialization())
            Objects.requireNonNull(this.client).setScreen(null);
        super.renderBackground(drawContext, mouseX, mouseY, delta);
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
