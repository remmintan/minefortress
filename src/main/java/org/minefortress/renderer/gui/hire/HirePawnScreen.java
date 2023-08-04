package org.minefortress.renderer.gui.hire;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.minefortress.network.c2s.C2SCloseHireMenuPacket;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.professions.hire.IHireScreenHandler;
import org.minefortress.renderer.gui.WindowScreen;
import org.minefortress.renderer.gui.widget.*;
import org.minefortress.utils.ModUtils;
import I;
import Z;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HirePawnScreen extends WindowScreen {

    private final List<HireButtonWithInfo> hireButtons = new ArrayList<>();

    private final IHireScreenHandler handler;

    public HirePawnScreen(@NotNull IHireScreenHandler handler) {
        super(new LiteralTextContent(handler.getScreenName()));
        this.handler = handler;
    }

    @Override
    protected void init() {
        super.init();

        final var professions = handler.getProfessions();
        hireButtons.clear();

        final var startY = getScreenTopY() + 40;
        addDrawable(new FreePawnsWidget(getScreenLeftX() + 10, startY - 15));
        var i = 0;
        for(final String profId : professions) {
            final var rowY = startY + i * 25;
            addNewRow(profId, rowY, getScreenLeftX(), getScreenRightX());
            i++;
        }
    }

    @Override
    public void tick() {
        super.tick();
        for (HireButtonWithInfo btn : hireButtons) {
            final var button = btn.button;
            final var profId = btn.profId;
            final var enoughPlaceForNew = (handler.getCurrentCount(profId) + handler.getHireQueue(profId)) < this.handler.getMaxCount(profId);
            button.active = btn.costs.isEnough() && ModUtils.getProfessionManager().getFreeColonists() > 0 && enoughPlaceForNew;
        }
    }

    private void addNewRow(String profId, int rowY, int leftX, int rightX) {
        this.addDrawable(
                new ProgressArrowWidget(
                        rightX - 48,
                        rowY,
                        () -> this.handler.getHireProgress(profId)
                )
        );
        final var professionName = new ProfessionNameWidget(
                IHireScreenHandler.getProfessionName(profId),
                leftX + 10,
                rowY + textRenderer.fontHeight / 2 + 3,
                this::renderTooltip
        );
        this.addDrawable(professionName);
        final var costs = new CostsWidget(
                leftX + professionName.getOffset() + 15,
                rowY,
                this.handler.getCost(profId)
        );
        this.addDrawable(costs);
        final var hireButton = new ButtonWidget(
                rightX - 100,
                rowY,
                20,
                20,
                new LiteralTextContent("+"),
                (btn) -> {
                    if(canIncreaseAmount(costs, profId)) {
                        this.handler.increaseAmount(profId);
                    }
                },
                (btn, matrices, x, y) -> {
                    final var buttonTooltip = canIncreaseAmount(costs, profId) ? "Hire" : "Not enough resources/pawns";
                    this.renderTooltip(matrices, new LiteralTextContent(buttonTooltip), x, y);
                }
        );
        this.addDrawableChild(hireButton);
        hireButtons.add(new HireButtonWithInfo(hireButton, costs, profId));
        this.addDrawable(
                new ProfessionQueueWidget(
                        rightX - 80,
                        rowY,
                        () -> handler.getHireQueue(profId),
                        this::renderTooltip
                )
        );

        this.addDrawable(
                new ProfessionAmountWidget(
                        rightX - 25,
                        rowY,
                        IHireScreenHandler.getProfessionItem(profId),
                        () -> this.handler.getCurrentCount(profId),
                        () -> this.handler.getMaxCount(profId),
                        this::renderTooltip
                )
        );
    }

    private boolean canIncreaseAmount(CostsWidget costs, String profId) {
        final var enoughPlaceForNew = (handler.getCurrentCount(profId) + handler.getHireQueue(profId)) < this.handler.getMaxCount(profId);
        return costs.isEnough() && ModUtils.getProfessionManager().getFreeColonists() > 0 && enoughPlaceForNew;
    }

    public IHireScreenHandler getHandler() {
        return handler;
    }

    @Override
    public void close() {
        super.close();
        final var packet = new C2SCloseHireMenuPacket();
        FortressClientNetworkHelper.send(C2SCloseHireMenuPacket.CHANNEL, packet);
    }

    private record HireButtonWithInfo(ButtonWidget button, CostsWidget costs, String profId) {}
}
