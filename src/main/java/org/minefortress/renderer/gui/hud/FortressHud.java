package org.minefortress.renderer.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.BlueprintsDimensionUtilsKt;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.gui.hud.HudState;
import net.remmintan.mods.minefortress.gui.hud.interfaces.IHintsLayer;
import net.remmintan.mods.minefortress.gui.hud.interfaces.IHudLayer;
import org.minefortress.renderer.gui.hud.hints.*;

import java.util.ArrayList;
import java.util.List;

public class FortressHud {

    private final MinecraftClient client;

    public final static int MOD_GUI_COLOR = 0xf0f0f0;

    private final List<IHintsLayer> hintsLayers = new ArrayList<>();
    private final List<IHudLayer> hudLayers = new ArrayList<>();

    public FortressHud(MinecraftClient client) {
        this.client = client;
        hintsLayers.add(new InitializationHintsLayer());
        hintsLayers.add(new BlueprintEditingHintsLayer());
        hintsLayers.add(new BuildBlueprintHintsLayer());
        hintsLayers.add(new BuildHintsLayer());
        hintsLayers.add(new BuildRoadsHintsLayer());
        hintsLayers.add(new ChopTreesHintsLayer());
        hintsLayers.add(new CombatHintsLayer());

        hudLayers.add(new ModeHudLayer(client));
        hudLayers.add(new ColonistsHudLayer(client));
        hudLayers.add(new SelectedColonistHudLayer(client));
        hudLayers.add(new HoveredEntityHudLayer(client));
        hudLayers.add(new ToolsHudLayer(client));
        hudLayers.add(new TimeHudLayer(client));
        hudLayers.add(new PawnsSelectionHudLayer(client));
        hudLayers.add(new CombatHudLayer(client));
        hudLayers.add(new AreasHudLayer(client));
        hudLayers.add(new UtilsHudLayer(client));
    }

    public void render(DrawContext drawContext, float delta) {
        if(isHudHidden()) return;
        prepareRenderSystem();

        final int scaledWidth = this.client.getWindow().getScaledWidth();
        final int scaledHeight = this.client.getWindow().getScaledHeight();

        int mouseX = (int)(this.client.mouse.getX() * (double) scaledWidth / (double)this.client.getWindow().getWidth());
        int mouseY = (int)(this.client.mouse.getY() * (double) scaledHeight / (double)this.client.getWindow().getHeight());

        final TextRenderer font = client.textRenderer;

        for(IHintsLayer layer : hintsLayers) {
            if(layer.shouldRender(getState())) {
                layer.render(drawContext, font, scaledWidth, scaledHeight, mouseX, mouseY, delta);
            }
        }

        for(IHudLayer layer : hudLayers) {
            if(layer.shouldRender(getState())) {
                layer.render(drawContext, font, scaledWidth, scaledHeight, mouseX, mouseY, delta);
            }
        }
    }

    private void prepareRenderSystem() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
    }

    public void tick() {
        for(IHudLayer layer : hudLayers) {
            if(layer.shouldRender(getState())) {
                layer.tick();
            }
        }
    }

    public boolean isHovered() {
        if(isHudHidden()) return false;
        for (IHudLayer layer : hudLayers) {
            if(layer.shouldRender(getState()) && layer.isHovered()) return true;
        }
        for (IHintsLayer layer : hintsLayers) { // Also check hints layers if they can be hovered
            if (layer.shouldRender(getState()) && layer.isHovered()) return true;
        }
        return false;
    }

    // New method to handle press
    public boolean onPress(double mouseX, double mouseY) {
        if (isHudHidden()) return false;
        for (IHudLayer layer : this.hudLayers) {
            if (layer.shouldRender(getState())) {
                if (layer.onHudPress(mouseX, mouseY)) {
                    return true; // Event consumed
                }
            }
        }
        return false; // Event not consumed
    }

    // Renamed from onClick to onRelease for clarity
    public void onRelease(double mouseX, double mouseY) {
        if(isHudHidden()) return;
        for(IHudLayer layer : this.hudLayers) {
            if(layer.shouldRender(getState())) {
                layer.onHudRelease(mouseX, mouseY);
            }
        }
    }


    private HudState getState() {
        final var fortressClientManager = ClientModUtils.getFortressManager();
        final var fortressCenterManager = ClientModUtils.getFortressCenterManager();
        if (fortressCenterManager.isCenterNotSet()) return HudState.INITIALIZING;
        if(fortressClientManager.notInitialized()) return HudState.BLANK;


        if (ClientModUtils.getBlueprintManager().isSelecting()) return HudState.BLUEPRINT;
        if (client.world != null && client.world.getRegistryKey() == BlueprintsDimensionUtilsKt.getBLUEPRINT_DIMENSION_KEY())
            return HudState.BLUEPRINT_EDITING;

        return switch (ClientModUtils.getFortressManager().getState()) {
            case BUILD_EDITING, BUILD_SELECTION -> HudState.BUILD;
            case COMBAT -> HudState.COMBAT;
            case AREAS_SELECTION -> HudState.AREAS_SELECTION;
        };
    }

    private boolean isHudHidden() {
        return client.options.hudHidden;
    }

}