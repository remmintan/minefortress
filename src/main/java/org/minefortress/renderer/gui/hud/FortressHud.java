package org.minefortress.renderer.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.minefortress.blueprints.manager.ClientBlueprintManager;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.hud.interfaces.IHudLayer;
import org.minefortress.selections.ClickType;
import org.minefortress.selections.SelectionManager;
import org.minefortress.selections.SelectionType;

import java.util.ArrayList;
import java.util.List;

public class FortressHud {

    private final MinecraftClient client;
    private SelectionManager selectionManager;

   private final static int MOD_GUI_COLOR = 0xf0f0f0;

   private final List<IHudLayer> layers = new ArrayList<>();

   private HudState state = HudState.BUILD;

    public FortressHud(MinecraftClient client) {
        this.client = client;
        layers.add(new ColonistsHudLayer(client));
        layers.add(new SelectedColonistHudLayer(client));
        layers.add(new ToolsHudLayer(client));
        layers.add(new TimeHudLayer(client));
        layers.add(new FightHudLayer(client));
    }

    private SelectionManager getSelectionManager() {
        if(this.selectionManager == null)
            this.selectionManager = getFortressClient().getSelectionManager();

        return this.selectionManager;
    }

    public void render(MatrixStack p, float delta) {
        if(isHudHidden()) return;
        final int scaledWidth = this.client.getWindow().getScaledWidth();
        final int scaledHeight = this.client.getWindow().getScaledHeight();

        int mouseX = (int)(this.client.mouse.getX() * (double) scaledWidth / (double)this.client.getWindow().getWidth());
        int mouseY = (int)(this.client.mouse.getY() * (double) scaledHeight / (double)this.client.getWindow().getHeight());

        final TextRenderer font = getTextRenderer();

        prepareRenderSystem();

        final FortressMinecraftClient fortressClient = getFortressClient();
        final FortressClientManager fortressManager = fortressClient.getFortressClientManager();


        if (fortressManager.isFortressInitializationNeeded()) {
            DrawableHelper.drawStringWithShadow(p, font, "Choose where to place your Fortress", 5, scaledHeight - font.fontHeight - 35, MOD_GUI_COLOR);
            DrawableHelper.drawStringWithShadow(p, font, "right click - set fortress center", 5, scaledHeight - font.fontHeight - 25, MOD_GUI_COLOR);
        } else if (client.world.getRegistryKey() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY && !this.isHovered()) { //FIXME very dirty implementation
            renderInfoText(p, font, "Editing blueprint");
            DrawableHelper.drawStringWithShadow(p, font, "esc - to save changes", 5, scaledHeight - font.fontHeight - 15, MOD_GUI_COLOR);
        } else {
            renderHints(p, scaledHeight, font);
            renderSelectTypeName(p, font);
            for(IHudLayer layer : layers) {
                if(layer.shouldRender(state)) {
                    layer.render(p, font, scaledWidth, scaledHeight, mouseX, mouseY, delta);
                }
            }
        }
    }

    private void renderHints(MatrixStack p, int scaledHeight, TextRenderer font) {
        if(getFortressClient().getFortressClientManager().isSelectingColonist()) return;

        if(getFortressClient().getFortressClientManager().isInCombat()) {
            DrawableHelper.drawStringWithShadow(p, font, "hold left mouse button and", 5, scaledHeight - font.fontHeight - 45, MOD_GUI_COLOR);
            DrawableHelper.drawStringWithShadow(p, font, "drag to select units", 5, scaledHeight - font.fontHeight - 35, MOD_GUI_COLOR);
            DrawableHelper.drawStringWithShadow(p, font, "click right mouse button", 5, scaledHeight - font.fontHeight - 25, MOD_GUI_COLOR);
            DrawableHelper.drawStringWithShadow(p, font, "to give commands", 5, scaledHeight - font.fontHeight - 15, MOD_GUI_COLOR);
            return;
        }

        final ClientBlueprintManager clientBlueprintManager = getBlueprintManager();
        if(clientBlueprintManager.hasSelectedBlueprint()) {
            final String selectedBlueprintName = clientBlueprintManager.getSelectedStructure().getName();
            renderInfoText(p, font, "Blueprint: " + selectedBlueprintName);

            DrawableHelper.drawStringWithShadow(p, font, "hold ctrl - keep blueprint", 5, scaledHeight - font.fontHeight - 45, MOD_GUI_COLOR);
            DrawableHelper.drawStringWithShadow(p, font, "ctrl + R - next blueprint", 5, scaledHeight - font.fontHeight - 35, MOD_GUI_COLOR);
            DrawableHelper.drawStringWithShadow(p, font, "ctrl + Q - rotate left", 5, scaledHeight - font.fontHeight - 25, MOD_GUI_COLOR);
            DrawableHelper.drawStringWithShadow(p, font, "ctrl + E - rotate right", 5, scaledHeight - font.fontHeight - 15, MOD_GUI_COLOR);
            return;
        }

        if(selectionManager != null) {
            final SelectionType selectionType = selectionManager.getCurrentSelectionType();
            if(selectionType == SelectionType.ROADS) {
                if(selectionManager.isSelecting()) {
                    DrawableHelper.drawStringWithShadow(p, font, "left click - cancel", 5, scaledHeight - font.fontHeight - 45, MOD_GUI_COLOR);
                    DrawableHelper.drawStringWithShadow(p, font, "right click - confirm task", 5, scaledHeight - font.fontHeight - 35, MOD_GUI_COLOR);
                    DrawableHelper.drawStringWithShadow(p, font, "ctrl + E - expand road", 5, scaledHeight - font.fontHeight - 25, MOD_GUI_COLOR);
                    DrawableHelper.drawStringWithShadow(p, font, "ctrl + Q - shrink road", 5, scaledHeight - font.fontHeight - 15, MOD_GUI_COLOR);
                } else {
                    DrawableHelper.drawStringWithShadow(p, font, "put any block in your hand", 5, scaledHeight - font.fontHeight - 35, MOD_GUI_COLOR);
                    DrawableHelper.drawStringWithShadow(p, font, "right click - start road", 5, scaledHeight - font.fontHeight - 25, MOD_GUI_COLOR);
                }
            } else if(selectionType == SelectionType.TREE) {
                if(selectionManager.isSelecting()) {
                    DrawableHelper.drawStringWithShadow(p, font, "left click - confirm task", 5, scaledHeight - font.fontHeight - 45, MOD_GUI_COLOR);
                    DrawableHelper.drawStringWithShadow(p, font, "right click - cancel", 5, scaledHeight - font.fontHeight - 35, MOD_GUI_COLOR);
                } else {
                    DrawableHelper.drawStringWithShadow(p, font, "left click - start tree", 5, scaledHeight - font.fontHeight - 25, MOD_GUI_COLOR);
                    DrawableHelper.drawStringWithShadow(p, font, "selection", 5, scaledHeight - font.fontHeight - 15, MOD_GUI_COLOR);
                }
            } else {
                if(selectionManager.isSelecting()) {
                    if(selectionManager.getClickType() == ClickType.REMOVE) {
                        DrawableHelper.drawStringWithShadow(p, font, "left click - confirm task", 5, scaledHeight - font.fontHeight - 45, MOD_GUI_COLOR);
                        DrawableHelper.drawStringWithShadow(p, font, "right click - cancel", 5, scaledHeight - font.fontHeight - 35, MOD_GUI_COLOR);
                    } else {
                        DrawableHelper.drawStringWithShadow(p, font, "left click - cancel", 5, scaledHeight - font.fontHeight - 45, MOD_GUI_COLOR);
                        DrawableHelper.drawStringWithShadow(p, font, "right click - confirm task", 5, scaledHeight - font.fontHeight - 35, MOD_GUI_COLOR);
                    }

                    DrawableHelper.drawStringWithShadow(p, font, "ctrl + E - move up", 5, scaledHeight - font.fontHeight - 25, MOD_GUI_COLOR);
                    DrawableHelper.drawStringWithShadow(p, font, "ctrl + Q - move down", 5, scaledHeight - font.fontHeight - 15, MOD_GUI_COLOR);
                } else {
                    DrawableHelper.drawStringWithShadow(p, font, "left click - dig", 5, scaledHeight - font.fontHeight - 25, MOD_GUI_COLOR);
                    DrawableHelper.drawStringWithShadow(p, font, "right click - build", 5, scaledHeight - font.fontHeight - 15, MOD_GUI_COLOR);
                }
            }
        }
    }

    private ClientBlueprintManager getBlueprintManager() {
        final FortressMinecraftClient client = getFortressClient();
        return client.getBlueprintManager();
    }

    private FortressMinecraftClient getFortressClient() {
        return (FortressMinecraftClient) this.client;
    }

    private void prepareRenderSystem() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }



    private void renderSelectTypeName(MatrixStack p, TextRenderer font) {
        if(getFortressClient().getFortressClientManager().isInCombat()) return;
        if(getFortressClient().getBlueprintManager().hasSelectedBlueprint())return;
        final SelectionManager selectionManager = getSelectionManager();
        String name = selectionManager.getCurrentSelectionType().getName();
        String selectionText = "Selection type: " + name;
        renderInfoText(p, font, selectionText);
    }

    private void renderInfoText(MatrixStack p, TextRenderer font, String selectionText) {
        DrawableHelper.drawStringWithShadow(p, font, selectionText, 5, 5, MOD_GUI_COLOR);
    }

    public void tick() {
        for(IHudLayer layer : layers) {
            if(layer.shouldRender(state)) {
                layer.tick();
            }
        }
    }

    public boolean isHovered() {
        if(isHudHidden()) return false;
        for (IHudLayer layer : layers) {
            if(layer.shouldRender(state) && layer.isHovered()) return true;
        }
        return false;
    }

    public void onClick(double mouseX, double mouseY) {
        if(isHudHidden()) return;
        for(IHudLayer layer : this.layers) {
            if(layer.shouldRender(state)) {
                layer.onClick(mouseX, mouseY);
            }
        }
    }

    private TextRenderer getTextRenderer() {
        return client.textRenderer;
    }

    private boolean isHudHidden() {
        return client.options.hudHidden || client.options.debugEnabled;
    }

}
