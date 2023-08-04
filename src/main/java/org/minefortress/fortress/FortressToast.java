package org.minefortress.fortress;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralTextContent;

public class FortressToast implements Toast {

    private Toast.Visibility visibility = Visibility.SHOW;

    private final LiteralTextContent title;
    private final LiteralTextContent descriptionFirstLine;
    private final ItemStack itemStack;

    public FortressToast(String title, String descriptionFirstLine, Item item) {
        this.title = new LiteralTextContent(title);
        this.descriptionFirstLine = new LiteralTextContent(descriptionFirstLine);
        this.itemStack = new ItemStack(item);
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        final TextRenderer textRenderer = manager.getClient().textRenderer;

        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        manager.drawTexture(matrices, 0, 0, 0, 96, this.getWidth(), this.getHeight());

        renderItem(manager);

        textRenderer.draw(matrices, this.title, 30.0f, 7.0f, -11534256);
        textRenderer.draw(matrices, this.descriptionFirstLine, 30.0f, 18.0f, -16777216);

        return visibility;
    }

    void hide() {
        this.visibility = Visibility.HIDE;
    }

    private void renderItem(ToastManager manager) {
        RenderSystem.enableBlend();
        final ItemRenderer itemRenderer = manager.getClient().getItemRenderer();
        itemRenderer.renderInGui(itemStack, 6, 6);
        RenderSystem.enableBlend();
    }

}
