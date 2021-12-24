package org.minefortress.renderer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.minefortress.entity.Colonist;

public class ColonistsGui extends FortressGuiScreen{

    private int colonistsCount = 0;

    protected ColonistsGui(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
    }

    @Override
    void tick() {
        if(client.world != null) {
            int count = 0;
            for(Entity entity: client.world.getEntities()) {
                if(entity instanceof Colonist) {
                    count++;
                }
            }
            colonistsCount = count;
        } else {
            colonistsCount = 0;
        }
    }

    @Override
    void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
//        super.drawTexture(p, 5, 15, 16, 0, 8, 8);
        super.itemRenderer.renderGuiItemIcon(new ItemStack(Items.PLAYER_HEAD), screenWidth/2 - 91, screenHeight - 40);
        font.draw(p,"x"+colonistsCount, screenWidth/2f - 91 + 15, screenHeight - 35, 0xFFFFFF);
    }
}
