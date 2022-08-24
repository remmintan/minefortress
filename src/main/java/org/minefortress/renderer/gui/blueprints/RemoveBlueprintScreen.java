package org.minefortress.renderer.gui.blueprints;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.minefortress.network.ServerboundEditBlueprintPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

public class RemoveBlueprintScreen extends Screen {

    private final String name;

    public RemoveBlueprintScreen(String name) {
        super(new LiteralText("Remove Blueprint"));
        this.name = name;
    }

    @Override
    protected void init() {
        super.init();
        final var yesBtn = new ButtonWidget(
                this.width / 2 - 102,
                this.height / 4 + 48 - 16,
                102,
                20,
                new LiteralText("Yes"),
                button -> {
                    final var removePacket = ServerboundEditBlueprintPacket.remove(name);
                    FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_UPDATE_BLUEPRINT, removePacket);
                }
        );
        final var noBtn = new ButtonWidget(
                this.width / 2,
                this.height / 4 + 48 - 16,
                102,
                20,
                new LiteralText("No"),
                button -> {
                    this.client.setScreen(new BlueprintsScreen());
                }
        );
        this.addDrawableChild(yesBtn);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        Screen.drawCenteredText(matrices, this.textRenderer, this.title, this.width/2, 40, 0xFFFFFF);
        Screen.drawCenteredText(matrices, this.textRenderer, new LiteralText(String.format("Do you want to delete blueprint: %s", name)), 60, 30, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
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
