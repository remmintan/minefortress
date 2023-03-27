package org.minefortress.renderer.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.minefortress.blueprints.world.BlueprintsWorld;
import org.minefortress.renderer.gui.hud.hints.*;
import org.minefortress.renderer.gui.hud.interfaces.IHintsLayer;
import org.minefortress.renderer.gui.hud.interfaces.IHudLayer;
import org.minefortress.utils.ModUtils;

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
        hudLayers.add(new FightHudLayer(client));
        hudLayers.add(new AreasHudLayer(client));
        hudLayers.add(new InluenceHudLayer(client));
    }

    public void render(MatrixStack p, float delta) {
        if(isHudHidden()) return;
        prepareRenderSystem();

        final int scaledWidth = this.client.getWindow().getScaledWidth();
        final int scaledHeight = this.client.getWindow().getScaledHeight();

        int mouseX = (int)(this.client.mouse.getX() * (double) scaledWidth / (double)this.client.getWindow().getWidth());
        int mouseY = (int)(this.client.mouse.getY() * (double) scaledHeight / (double)this.client.getWindow().getHeight());

        final TextRenderer font = client.textRenderer;

        for(IHintsLayer layer : hintsLayers) {
            if(layer.shouldRender(getState())) {
                layer.render(p, font, scaledWidth, scaledHeight, mouseX, mouseY, delta);
            }
        }

        for(IHudLayer layer : hudLayers) {
            if(layer.shouldRender(getState())) {
                layer.render(p, font, scaledWidth, scaledHeight, mouseX, mouseY, delta);
            }
        }
    }

    private void prepareRenderSystem() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
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
        return false;
    }

    public void onClick(double mouseX, double mouseY) {
        if(isHudHidden()) return;
        for(IHudLayer layer : this.hudLayers) {
            if(layer.shouldRender(getState())) {
                layer.onClick(mouseX, mouseY);
            }
        }
    }

    private HudState getState() {
        final var fortressClientManager = ModUtils.getFortressClientManager();
        if(fortressClientManager.notInitialized()) return HudState.BLANK;
        if(fortressClientManager.isCenterNotSet()) return HudState.INITIALIZING;

        if(ModUtils.getBlueprintManager().isSelecting()) return HudState.BLUEPRINT;
        if(BlueprintsWorld.isBlueprintsWorld(client.world)) return HudState.BLUEPRINT_EDITING;

        return switch (ModUtils.getFortressClientManager().getState()) {
            case BUILD -> HudState.BUILD;
            case COMBAT -> HudState.COMBAT;
            case AREAS_SELECTION -> HudState.AREAS_SELECTION;
        };
    }

    private boolean isHudHidden() {
        return client.options.hudHidden || client.options.debugEnabled;
    }

}
