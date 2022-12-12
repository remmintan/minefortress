package org.minefortress.renderer.gui.hire;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.NotNull;
import org.minefortress.professions.hire.IHireScreenHandler;
import org.minefortress.renderer.gui.WindowScreen;
import org.minefortress.renderer.gui.widget.CostsWidget;
import org.minefortress.renderer.gui.widget.ProfessionAmountWidget;
import org.minefortress.renderer.gui.widget.ProgressArrowWidget;

public class HirePawnScreen extends WindowScreen {

    private final IHireScreenHandler handler;

    public HirePawnScreen(@NotNull IHireScreenHandler handler) {
        super(new LiteralText(handler.getName()));
        this.handler = handler;
    }

    @Override
    protected void init() {
        super.init();

        final var professions = handler.getProfessions();
        final var startY = getScreenTopY() + 25;
        var i = 0;
        for(final String profId : professions) {
            final var rowY = startY + i * 10;
            this.addDrawable(new ProgressArrowWidget(getScreenCenterX(), rowY, () -> this.handler.getHireProgress(profId)));
            this.addDrawable(new CostsWidget(getScreenLeftX() + 10, rowY, this.handler.getCost(profId)));
            this.addDrawableChild(
                new ButtonWidget(
                    getScreenCenterX() + 15,
                    rowY,
                    20,
                    20,
                    new LiteralText("+"),
                    (btn) -> this.handler.increaseAmount(profId)
                )
            );
            this.addDrawable(
                    new ProfessionAmountWidget(getScreenRightX() - 15, rowY, this.handler.getProfessionalHeadItem(profId), () -> this.handler.getCurrentCount(profId))
            );

            i++;
        }
    }
}
