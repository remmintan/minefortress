package org.minefortress.renderer.gui.hire;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.minefortress.network.c2s.C2SCloseHireMenuPacket;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.professions.hire.IHireScreenHandler;
import org.minefortress.renderer.gui.WindowScreen;
import org.minefortress.renderer.gui.widget.*;
import org.minefortress.utils.ModUtils;

import java.util.ArrayList;
import java.util.List;

public class HirePawnScreen extends WindowScreen {

    private final List<Pair<CostsWidget, ButtonWidget>> hireButtons = new ArrayList<>();

    private final IHireScreenHandler handler;

    public HirePawnScreen(@NotNull IHireScreenHandler handler) {
        super(new LiteralText(handler.getScreenName()));
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
        for (Pair<CostsWidget, ButtonWidget> btn : hireButtons) {
            final var button = btn.getRight();
            button.active = btn.getLeft().isEnough() && ModUtils.getProfessionManager().getFreeColonists() > 0;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        HirePawnScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.getScreenCenterX(), this.getScreenTopY() + 10, 0xFFFFFF);
    }

    private void addNewRow(String profId, int rowY, int leftX, int rightX) {
        this.addDrawable(
                new ProgressArrowWidget(
                        rightX - 38,
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
                rightX - 90,
                rowY,
                20,
                20,
                new LiteralText("+"),
                (btn) -> {
                    if(canIncreaseAmount(costs)) {
                        this.handler.increaseAmount(profId);
                    }
                },
                (btn, matrices, x, y) -> {
                    final var buttonTooltip = canIncreaseAmount(costs) ? "Hire" : "Not enough resources/pawns";
                    this.renderTooltip(matrices, new LiteralText(buttonTooltip), x, y);
                }
        );
        this.addDrawableChild(hireButton);
        hireButtons.add(new Pair<>(costs, hireButton));
        this.addDrawable(
                new ProfessionQueueWidget(
                        rightX - 70,
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
                        () -> this.handler.getCurrentCount(profId)
                )
        );
    }

    private static boolean canIncreaseAmount(CostsWidget costs) {
        return costs.isEnough() && ModUtils.getProfessionManager().getFreeColonists() > 0;
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
}
