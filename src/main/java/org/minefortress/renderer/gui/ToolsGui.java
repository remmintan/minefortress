package org.minefortress.renderer.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.widget.FortressItemButtonWidget;
import org.minefortress.selections.SelectionType;

import java.util.ArrayList;
import java.util.List;

public class ToolsGui extends FortressGuiScreen {

    private final FortressItemButtonWidget selectionType;
    private final ButtonWidget questionButton;
    private final List<ButtonWidget> selectionButtons = new ArrayList<>();

    protected ToolsGui(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
        this.selectionType = new FortressItemButtonWidget(
                0,
                0,
                Items.DIAMOND_PICKAXE,
                itemRenderer,
                btn -> {
                    final FortressItemButtonWidget fortressBtn = (FortressItemButtonWidget) btn;
                    fortressBtn.checked = !fortressBtn.checked;
                },
                (button, matrices, mouseX, mouseY) -> {
                    final FortressItemButtonWidget fortressButton = (FortressItemButtonWidget) button;
                    if(!fortressButton.checked) {
                        ToolsGui.super.renderTooltip(matrices, new LiteralText("Selection Type"), mouseX, mouseY);
                    }
                },
                Text.of("")
        );
        this.questionButton = new ButtonWidget(0, 0, 20, 20, new LiteralText("?"), btn -> {
            this.selectionType.checked = false;
            final BookScreen questionsScreen = new BookScreen(new FortressBookContents(FortressBookContents.HELP_BOOK));

            this.client.setScreen(questionsScreen);
        });

        final FortressMinecraftClient fortressClient = (FortressMinecraftClient) client;
        for(final SelectionType type : SelectionType.values()) {
            this.selectionButtons.add(
                    new ButtonWidget(0, 0, 20, 20, new LiteralText(type.getButtonText()), btn -> {
                        fortressClient.getSelectionManager().setSelectionType(type);
                    }, (button, matrices, mouseX, mouseY) -> renderTooltip(matrices, new LiteralText(type.getName()), mouseX, mouseY))
            );
        }
    }

    @Override
    void tick() {
        if(this.selectionType.checked) {
            this.selectionButtons.forEach(b -> b.visible = true);
        } else {
            this.selectionButtons.forEach(b -> b.visible = false);
        }
    }

    @Override
    void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        this.selectionType.setPos(screenWidth - 25, 5);
        this.selectionType.render(p, (int)mouseX, (int)mouseY, delta);

        for (int i = 0; i < selectionButtons.size(); i++) {
            ButtonWidget btn = selectionButtons.get(i);
            btn.x = screenWidth - 60;
            btn.y = i * 25 + 5;

            btn.render(p, (int)mouseX, (int)mouseY, delta);
        }

        this.questionButton.x = screenWidth - 25;
        this.questionButton.y = 35;
        this.questionButton.render(p, (int)mouseX, (int)mouseY, delta);
    }

    @Override
    boolean isHovered() {
        return this.selectionType.isHovered() || this.questionButton.isHovered() || this.selectionButtons.stream().anyMatch(btn -> btn.visible && btn.isHovered());
    }

    @Override
    void onClick(double mouseX, double mouseY) {
        this.selectionType.onClick(mouseX, mouseY);
        if(questionButton.isHovered())
            this.questionButton.onClick(mouseX, mouseY);

        for (ButtonWidget btn : selectionButtons) {
            if (btn.visible && btn.isHovered())
                btn.onClick(mouseX, mouseY);
        }
    }
}
