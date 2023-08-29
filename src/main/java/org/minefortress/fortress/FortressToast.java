package org.minefortress.fortress;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class FortressToast implements Toast {

    private Toast.Visibility visibility = Visibility.SHOW;

    private final Text title;
    private final Text descriptionFirstLine;
    private final ItemStack itemStack;

    public FortressToast(String title, String descriptionFirstLine, Item item) {
        this.title = Text.literal(title);
        this.descriptionFirstLine = Text.literal(descriptionFirstLine);
        this.itemStack = new ItemStack(item);
    }

    @Override
    public Visibility draw(DrawContext drawContext, ToastManager manager, long startTime) {
        final TextRenderer textRenderer = manager.getClient().textRenderer;

        drawContext.drawTexture(TEXTURE, 0, 0, 0, 96, this.getWidth(), this.getHeight());

        renderItem(drawContext);

        drawContext.drawText(textRenderer, this.title, 30, 7, 0xff500050, false);
        drawContext.drawText(textRenderer, this.descriptionFirstLine, 30, 18, 0xff000000, false);

        return visibility;
    }

    void hide() {
        this.visibility = Visibility.HIDE;
    }

    private void renderItem(DrawContext drawContext) {
        RenderSystem.enableBlend();
        drawContext.drawItem(itemStack, 6, 6);
        RenderSystem.disableBlend();
    }

}
