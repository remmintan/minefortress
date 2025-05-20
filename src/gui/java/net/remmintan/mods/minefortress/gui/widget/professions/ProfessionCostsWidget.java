package net.remmintan.mods.minefortress.gui.widget.professions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.gui.util.GuiUtils;

import java.util.List;
import java.util.stream.Stream;

public class ProfessionCostsWidget implements Drawable, Element {

    private final int x;
    private final int y;
    private final List<ItemInfo> costs;

    public ProfessionCostsWidget(int x, int y, List<ItemInfo> costs) {
        this.x = x;
        this.y = y;
        this.costs = costs;
    }

    private static int getItemAmountIncludingSimilars(Item item) { // Changed from getItemAmount
        final var fortressClientManager = ClientModUtils.getFortressManager();
        return fortressClientManager.getResourceManager().getCountIncludingSimilars(item);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        int i = 0;

        for(var ent : costs) {
            final var stack = ent.item().getDefaultStack();
            final var requiredAmount = ent.amount();
            final var totalAvailableForThisType = getItemAmountIncludingSimilars(ent.item());
            final var color = totalAvailableForThisType >= requiredAmount ? 0xFFFFFF : 0xFF0000; // Color based on overall check

            final var countLabel = requiredAmount + "/" + GuiUtils.formatSlotCount(totalAvailableForThisType);
            final var textRenderer = getTextRenderer();
            final var countLabelWidth = textRenderer.getWidth(countLabel);
            final var matrices = drawContext.getMatrices();
            matrices.push();
            final var scaleFactor = 0.5f;
            matrices.scale(scaleFactor, scaleFactor, 1f);
            matrices.translate(10, 0, 250);

            final var textX = x + i + countLabelWidth / 2f - 25 * scaleFactor;
            final var textY = y + 6 / scaleFactor;
            drawContext.drawText(getTextRenderer(), countLabel, (int)(textX / scaleFactor), (int)(textY / scaleFactor), color, false);
            matrices.pop();
            drawContext.drawItem(stack, x + i, y);
            i += (int) ((25 + countLabelWidth) * scaleFactor);
        }

        var endX = x + i;
        var endY = y + 25;

        if (mouseX >= x && mouseX <= endX && mouseY >= y && mouseY <= endY) {
            final var tooltip = Stream.of("Recruitment Costs. Displays the resources", "required to hire a new unit.").map(Text::of).toList();
            drawContext.drawTooltip(getTextRenderer(), tooltip, mouseX, mouseY);
        }
    }

    private static TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }
}
