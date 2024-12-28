package net.remmintan.mods.minefortress.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.dtos.ItemInfo;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.gui.util.GuiUtils;

import java.util.List;

public class CostsWidget implements Drawable, Element {

    private final int x;
    private final int y;
    private final List<ItemInfo> costs;

    public CostsWidget(int x, int y, List<ItemInfo> costs) {
        this.x = x;
        this.y = y;
        this.costs = costs;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        int i = 0;
        for(var ent : costs) {
            final var stack = ent.item().getDefaultStack();
            final var amount = ent.amount();
            final var actualItemAmount = getItemAmount(stack);
            final var color = actualItemAmount >= amount ? 0xFFFFFF : 0xFF0000;
            final var countLabel = amount + "/" + GuiUtils.formatSlotCount(actualItemAmount);
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
            final var tooltip = List.of("Recruitment Costs. Displays the resources", "required to hire a new unit.").stream().map(Text::of).toList();
            drawContext.drawTooltip(getTextRenderer(), tooltip, mouseX, mouseY);
        }
    }

    public boolean isEnough() {
        for(var ent : costs) {
            final var stack = ent.item().getDefaultStack();
            final var amount = ent.amount();
            final var actualItemAmount = getItemAmount(stack);
            if(actualItemAmount < amount) {
                return false;
            }
        }
        return true;
    }

    public List<ItemInfo> getCosts() {
        return costs;
    }

    private static int getItemAmount(ItemStack stack) {
        final var fortressClientManager = CoreModUtils.getFortressManager();
        return fortressClientManager.getResourceManager().getItemAmount(stack.getItem());
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
