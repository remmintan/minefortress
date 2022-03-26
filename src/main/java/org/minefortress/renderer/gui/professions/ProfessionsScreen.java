package org.minefortress.renderer.gui.professions;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.minefortress.interfaces.FortressMinecraftClient;

public class ProfessionsScreen extends Screen {

    private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");

    private static final int WINDOW_WIDTH = 252;
    private static final int WINDOW_HEIGHT = 140;

    private final ProfessionsLayer professionsLayer;

    public ProfessionsScreen(FortressMinecraftClient client) {
        super(new LiteralText("Professions"));
        this.professionsLayer = new ProfessionsLayer(client);
    }

    @Override
    protected void init() {

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int screenX = (this.width - WINDOW_WIDTH) / 2;
        int screenY = (this.height - WINDOW_HEIGHT) / 2;
        this.renderBackground(matrices);
        this.drawProfessionsTree(matrices, mouseX, mouseY, screenX, screenY);
        this.drawWindowAndTitle(matrices, screenX, screenY);
    }

    private void drawProfessionsTree(MatrixStack matrices, int mouseX, int mouseY, int screenX, int screenY) {
        MatrixStack i = RenderSystem.getModelViewStack();
        i.push();
        i.translate(screenX + 9, screenY + 18, 0.0);
        RenderSystem.applyModelViewMatrix();
        professionsLayer.render(matrices);
        i.pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableDepthTest();
    }

    private void drawWindowAndTitle(MatrixStack matrices, int screenX, int screenY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
        this.drawTexture(matrices, screenX, screenY, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        this.textRenderer.draw(matrices, this.title, (float)(screenX + 8), (float)(screenY + 6), 0x404040);
    }

}
