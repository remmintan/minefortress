package org.minefortress.renderer.gui.blueprints;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.apache.logging.log4j.util.Strings;
import org.minefortress.network.c2s.ServerboundEditBlueprintPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;

public class AddBlueprintScreen extends Screen {

    private final BlueprintGroup group;

    public AddBlueprintScreen(BlueprintGroup group) {
        super(new LiteralText("Add new Blueprint"));
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();
        final var textField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 102,
                this.height / 4 + 24 - 16, 204, 20,
                new LiteralText("Blueprint Name")
        );
        this.addDrawableChild(textField);
        final var btn = new ButtonWidget(
                this.width / 2 - 102,
                this.height / 4 + 48 - 16, 204, 20,
                new LiteralText("Save blueprint"),
                button -> {
                    final var text = textField.getText();
                    if(Strings.isNotBlank(text)) {
                        final var packet = ServerboundEditBlueprintPacket.add(text, group);
                        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_EDIT_BLUEPRINT, packet);
                        if(this.client != null) this.client.setScreen(null);

                    }
                }

        );
        this.addDrawableChild(btn);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        Screen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF);
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
