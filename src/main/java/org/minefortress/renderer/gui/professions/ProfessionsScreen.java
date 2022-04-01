package org.minefortress.renderer.gui.professions;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.lwjgl.opengl.GL11;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.professions.ProfessionManager;

public class ProfessionsScreen extends Screen {

    private final ProfessionsLayer professionsLayer;
    private final FortressClientManager fortressManager;
    private final ProfessionManager professionManager;

    private boolean movingLayer = false;
    private boolean startClick = false;

    public ProfessionsScreen(FortressMinecraftClient client) {
        super(new LiteralText("Professions"));
        this.professionsLayer = new ProfessionsLayer(client);
        this.fortressManager = client.getFortressClientManager();
        this.professionManager = client.getFortressClientManager().getProfessionManager();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
//        int screenX = (this.width - WINDOW_WIDTH) / 2;
//        int screenY = (this.height - WINDOW_HEIGHT) / 2;
        int screenX = 0;
        int screenY = 0;
        this.renderBackground(matrices);

        this.drawProfessionsTree(matrices, mouseX, mouseY, screenX, screenY);
        this.textRenderer.draw(matrices, this.title, (float)(screenX + 8), (float)(screenY + 6), 0xFFFFFF);

        matrices.push();
        matrices.translate(0, 0, 200.0);
        final String availableColonistsText = String.format("Available colonists: %d/%d", professionManager.getFreeColonists(), fortressManager.getTotalColonistsCount());
        this.textRenderer.draw(matrices, availableColonistsText, screenX + 9, screenY + 18, 0xFFFFFF);
        this.textRenderer.draw(matrices, "left click on profession - add pawn to profession", 2, this.height - (2*this.textRenderer.fontHeight) - 2, 0xffffff);
        this.textRenderer.draw(matrices, "right click on profession - remove pawn from profession", 2, this.height -  this.textRenderer.fontHeight - 2, 0xffffff);
        matrices.pop();

        this.drawWidgetTooltip(matrices, mouseX, mouseY, screenX, screenY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.startClick = false;
        if (button != 0) {
            this.movingLayer = false;
            return false;
        }
        if (!this.movingLayer) {
            this.movingLayer = true;
        } else {
            this.professionsLayer.move(deltaX, deltaY);
        }
        return true;
    }

    private void drawProfessionsTree(MatrixStack matrices, int mouseX, int mouseY, int screenX, int screenY) {
        MatrixStack i = RenderSystem.getModelViewStack();
        i.push();
        i.translate(screenX, screenY, 0.0);
        RenderSystem.applyModelViewMatrix();
        professionsLayer.setLayerSizes(this.width, this.height);
        professionsLayer.render(matrices);
        i.pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableDepthTest();
    }

    private void drawWidgetTooltip(MatrixStack matrices, int mouseX, int mouseY, int screenX, int screenY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(screenX, screenY, 400.0);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
        this.professionsLayer.drawWidgetTooltip(matrices, mouseX - screenX, mouseY - screenY, screenX, screenY, width);
        RenderSystem.disableDepthTest();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.startClick = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(startClick) {
            this.professionsLayer.onClick(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }
}
