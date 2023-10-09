package org.minefortress.renderer.gui.blueprints;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundFinishEditBlueprintPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;

@Environment(value= EnvType.CLIENT)
public class BlueprintsPauseScreen extends Screen {

    private final boolean showMenu;

    public BlueprintsPauseScreen(boolean showMenu) {
        super(Text.of("Edit Blueprint"));
        this.showMenu = showMenu;
    }

    @Override
    protected void init() {
        if (this.showMenu) {
            this.initWidgets();
        }
    }

    private void initWidgets() {
        final var backBtn = ButtonWidget
                .builder(Text.literal("Back to game"), button -> closeMenu())
                .dimensions(this.width / 2 - 102, this.height / 4 + 24 - 16, 204, 20)
                .build();
        this.addDrawableChild(backBtn);

        final var saveBtn = ButtonWidget
                .builder(Text.literal("Save blueprint"), button -> {
                    sendSave(true);
                    closeMenu();
                })
                .dimensions(this.width / 2 - 102, this.height / 4 + 48 - 16, 204, 20)
                .build();
        this.addDrawableChild(saveBtn);

        final var clearBtn = ButtonWidget
                .builder(Text.literal("Clear blueprint"), button -> {
                    openClearConfirmationScreen();
                })
                .dimensions(this.width / 2 - 102, this.height / 4 + 72 - 16, 204, 20)
                .build();
        this.addDrawableChild(clearBtn);

        final var discardBtn = ButtonWidget
                .builder(Text.literal("Discard Blueprint"), button -> {
                    sendSave(false);
                    closeMenu();
                })
                .dimensions(this.width / 2 - 102, this.height / 4 + 96 - 16, 204, 20)
                .build();
        this.addDrawableChild(discardBtn);
    }

    private void sendSave(boolean shouldSave) {
        final ServerboundFinishEditBlueprintPacket packet = new ServerboundFinishEditBlueprintPacket(shouldSave);
        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SAVE_EDIT_BLUEPRINT, packet);
    }

    private void openClearConfirmationScreen() {
        if(this.client!=null)
            this.client.setScreen(new ClearBlueprintConfirmationScreen(this));
        else
            throw new RuntimeException("Client is null");
    }

    private void closeMenu() {
        this.client.setScreen(null);
        this.client.mouse.lockCursor();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (this.showMenu) {
            this.renderBackground(drawContext);
            drawContext.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF);
        } else {
            drawContext.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        }
        super.render(drawContext, mouseX, mouseY, delta);
    }

}
