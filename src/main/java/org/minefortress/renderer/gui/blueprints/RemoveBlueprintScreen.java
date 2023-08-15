package org.minefortress.renderer.gui.blueprints;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.minefortress.network.c2s.ServerboundEditBlueprintPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

public class RemoveBlueprintScreen extends Screen {

    private final String name;

    public RemoveBlueprintScreen(String name) {
        super(Text.literal("Remove Blueprint"));
        this.name = name;
    }

    @Override
    protected void init() {
        super.init();
        final var yesBtn = ButtonWidget
                .builder(
                        Text.literal("Yes"),
                        button -> {
                            final var removePacket = ServerboundEditBlueprintPacket.remove(name);
                            FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_UPDATE_BLUEPRINT, removePacket);
                        }
                )
                .dimensions(this.width / 2 - 102, this.height / 4 + 48 - 16, 102, 20)
                .build();
        this.addDrawableChild(yesBtn);
        final var noBtn = ButtonWidget
                .builder(
                        Text.literal("No"),
                        button -> {
                            this.client.setScreen(new BlueprintsScreen());
                        }
                )
                .dimensions(this.width / 2, this.height / 4 + 48 - 16, 102, 20)
                .build();
        this.addDrawableChild(noBtn);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 40, 0xFFFFFF);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, Text.literal(String.format("Do you want to delete blueprint: %s", name)), 60, 30, 0xFFFFFF);
        super.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if(this.client != null) this.client.setScreen(new BlueprintsScreen());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
