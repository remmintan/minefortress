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
import org.minefortress.selections.ClickType;
import org.minefortress.selections.SelectionManager;
import org.minefortress.selections.SelectionType;

public class FortressHud {

    private final MinecraftClient client;
    private SelectionManager selectionManager;

   private final static int MOD_GUI_COLOR = 0xf0f0f0;
   private final static boolean SHOW_WATER_MARKS = false;
   private final static boolean REDDIT_WATERMARKS_ENABLED = false;

   private final ColonistsHudLayer colonistsHudLayer;
   private final ToolsHudLayer toolsHudLayer;
   private final TimeHudLayer timeHudLayer;
   private final FightHudLayer fightHudLayer;

   private boolean isHovered = false;

    public FortressHud(MinecraftClient client) {
        this.client = client;
        this.colonistsHudLayer = new ColonistsHudLayer(client, client.getItemRenderer());
        this.toolsHudLayer = new ToolsHudLayer(client, client.getItemRenderer());
        this.timeHudLayer = new TimeHudLayer(client, client.getItemRenderer());
        this.fightHudLayer = new FightHudLayer(client, client.getItemRenderer());
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

        renderWatermarks(p, font, scaledWidth, scaledHeight);

        if (fortressManager.isFortressInitializationNeeded()) {
            DrawableHelper.drawStringWithShadow(p, font, "Choose where to place your Fortress", 5, scaledHeight - font.fontHeight - 35, MOD_GUI_COLOR);
            DrawableHelper.drawStringWithShadow(p, font, "right click - set fortress center", 5, scaledHeight - font.fontHeight - 25, MOD_GUI_COLOR);
        } else if (client.world.getRegistryKey() == BlueprintsWorld.BLUEPRINTS_WORLD_REGISTRY_KEY && !this.isHovered()) { //FIXME very dirty implementation
            renderInfoText(p, font, "Editing blueprint");
            DrawableHelper.drawStringWithShadow(p, font, "esc - to save changes", 5, scaledHeight - font.fontHeight - 15, MOD_GUI_COLOR);
        } else {
            renderHints(p, scaledHeight, font);
            renderSelectTypeName(p, font);
            this.colonistsHudLayer.render(p, font, scaledWidth, scaledHeight, mouseX, mouseY, delta);
            this.toolsHudLayer.render(p, font, scaledWidth, scaledHeight, mouseX, mouseY, delta);
            this.timeHudLayer.render(p, font, scaledWidth, scaledHeight, mouseX, mouseY, delta);
            if(fortressManager.isInCombat())
                fightHudLayer.render(p, font, scaledWidth, scaledHeight, mouseX, mouseY, delta);
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

    private void renderWatermarks(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight) {
        int y = screenHeight - font.fontHeight - 5;

        if(SHOW_WATER_MARKS) {
            if(REDDIT_WATERMARKS_ENABLED) {
                DrawableHelper.drawStringWithShadow(p, font, "/u/remmintan", 5, y, MOD_GUI_COLOR);
            } else {
                DrawableHelper.drawStringWithShadow(p, font, "Minecraft Fortress Mod", 5, y, MOD_GUI_COLOR);
                DrawableHelper.drawStringWithShadow(p, font, "minecraftfortress.org", screenWidth - font.getWidth("minecraftfortress.org") - 5, y, MOD_GUI_COLOR);
            }
        }
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
        this.colonistsHudLayer.tick();
        this.toolsHudLayer.tick();

        this.isHovered = this.colonistsHudLayer.isHovered() || this.toolsHudLayer.isHovered() || this.timeHudLayer.isHovered();
    }

    public boolean isHovered() {
        if(isHudHidden()) return false;
        return this.isHovered;
    }

    public void onClick(double mouseX, double mouseY) {
        if(isHudHidden()) return;
        this.toolsHudLayer.onClick(mouseX, mouseY);
        this.colonistsHudLayer.onClick(mouseX, mouseY);
        this.timeHudLayer.onClick(mouseX, mouseY);
    }

    private TextRenderer getTextRenderer() {
        return client.textRenderer;
    }

    private boolean isHudHidden() {
        return client.options.hudHidden || client.options.debugEnabled;
    }

}
