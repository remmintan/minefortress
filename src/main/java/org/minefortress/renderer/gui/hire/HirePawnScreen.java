package org.minefortress.renderer.gui.hire;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.NotNull;
import org.minefortress.professions.hire.IHireScreenHandler;
import org.minefortress.renderer.gui.WindowScreen;
import org.minefortress.renderer.gui.widget.*;

public class HirePawnScreen extends WindowScreen {

    private final IHireScreenHandler handler;

    public HirePawnScreen(@NotNull IHireScreenHandler handler) {
        super(new LiteralText(handler.getScreenName()));
        this.handler = handler;
    }

    @Override
    protected void init() {
        super.init();

        final var professions = handler.getProfessions();
        final var startY = getScreenTopY() + 25;
        var i = 0;
        for(final String profId : professions) {
            final var rowY = startY + i * 25;
            addNewRow(profId, rowY, getScreenLeftX(), getScreenRightX());
            i++;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        HirePawnScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.getScreenCenterX(), this.getScreenTopY() + 5, 0xFFFFFF);
    }

    private void addNewRow(String profId, int rowY, int leftX, int rightX) {
        final var professionName = new ProfessionNameWidget(
                IHireScreenHandler.getProfessionName(profId),
                leftX + 10,
                rowY + textRenderer.fontHeight / 2 + 3,
                this::renderTooltip
        );
        this.addDrawable(professionName);
        this.addDrawable(
                new CostsWidget(
                        leftX + professionName.getOffset() + 25,
                        rowY,
                        this.handler.getCost(profId)
                )
        );
        this.addDrawableChild(
                new ButtonWidget(
                        rightX - 90,
                        rowY,
                        20,
                        20,
                        new LiteralText("+"),
                        (btn) -> this.handler.increaseAmount(profId)
                )
        );
        this.addDrawable(
                new ProfessionQueueWidget(
                        rightX - 70,
                        rowY,
                        () -> 10
                )
        );
        this.addDrawable(
                new ProgressArrowWidget(
                        rightX - 38,
                        rowY,
                        () -> this.handler.getHireProgress(profId)
                )
        );
        this.addDrawable(
                new ProfessionAmountWidget(
                        rightX - 25,
                        rowY,
                        IHireScreenHandler.getProfessionItem(profId),
                        () -> this.handler.getCurrentCount(profId)
                )
        );
    }

}
