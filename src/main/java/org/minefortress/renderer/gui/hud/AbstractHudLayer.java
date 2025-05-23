package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.remmintan.mods.minefortress.core.utils.ClientExtensionsKt;
import net.remmintan.mods.minefortress.gui.hud.interfaces.IHudButton;
import net.remmintan.mods.minefortress.gui.hud.interfaces.IHudElement;
import net.remmintan.mods.minefortress.gui.hud.interfaces.IHudLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractHudLayer implements IHudLayer {

    private final List<IHudElement> fortressHudElements = new ArrayList<>();

    protected final MinecraftClient client;
    protected final TextRenderer textRenderer;

    private Integer basepointX;
    private Integer basepointY;
    private PositionX positionX;
    private PositionY positionY;

    private boolean initialized = false;
    private IHudElement pressedElement = null; // Stores the element that was pressed

    protected AbstractHudLayer(MinecraftClient client) {
        this.client = client;
        this.textRenderer = client.textRenderer;
    }

    protected void init() {}

    protected final void setBasepoint(int x, int y, PositionX positionX, PositionY positionY) {
        this.basepointX = x;
        this.basepointY = y;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    protected final void addElement(IHudElement... buttons) {
        fortressHudElements.addAll(Arrays.asList(buttons));
    }

    public void tick() {
        if(!initialized) {
            init();
            initialized = true;
        }
        for(IHudElement button : fortressHudElements)
            button.tick();
    }

    @Override
    final public void render(DrawContext drawContext, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        if(basepointX == null || basepointY == null){
            throw new IllegalStateException("Basepoint not set!");
        }
        this.renderHud(drawContext, screenWidth, screenHeight);

        final var baseX = switch (positionX) {
            case LEFT -> basepointX;
            case RIGHT -> screenWidth + basepointX;
            case CENTER -> screenWidth / 2 + basepointX;
        };

        final var baseY = switch (positionY) {
            case TOP -> basepointY;
            case BOTTOM -> screenHeight + basepointY;
            case CENTER -> screenHeight / 2 + basepointY;
        };

        boolean creative = ClientExtensionsKt.isCreativeFortress(MinecraftClient.getInstance());
        for (IHudElement fortressHudButton : fortressHudElements) {
            fortressHudButton.setPosBasedOn(baseX, baseY);
            if(fortressHudButton.shouldRender(creative)) {
                fortressHudButton.render(drawContext, (int)mouseX, (int)mouseY, delta);
            }
        }
    }

    protected void renderHud(DrawContext drawContext, int screenWidth, int screenHeight) {}

    final public boolean isHovered() {
        boolean creative = ClientExtensionsKt.isCreativeFortress(MinecraftClient.getInstance());
        for (IHudElement fortressHudButton : fortressHudElements) {
            if (fortressHudButton.shouldRender(creative) && fortressHudButton.isHovered()) return true;
        }
        return false;
    }

    @Override
    public boolean onHudPress(double mouseX, double mouseY) {
        boolean creative = ClientExtensionsKt.isCreativeFortress(MinecraftClient.getInstance());
        for (IHudElement element : fortressHudElements) {
            if (element.shouldRender(creative) && element.isHovered() && element instanceof IHudButton) {
                this.pressedElement = element;
                // Potentially call a onPress method on the button itself if needed for visual feedback
                return true; // Event consumed by this layer
            }
        }
        return false; // Event not consumed
    }

    @Override
    public void onHudRelease(double mouseX, double mouseY) {
        boolean creative = ClientExtensionsKt.isCreativeFortress(MinecraftClient.getInstance());
        if (this.pressedElement != null) {
            if (this.pressedElement.shouldRender(creative) && this.pressedElement.isHovered() && this.pressedElement instanceof IHudButton button) {
                button.onClick(mouseX, mouseY); // Execute the actual click action
            }
            this.pressedElement = null; // Reset pressed element regardless of whether the click was successful
        }
    }


    public enum PositionX {
        LEFT, RIGHT, CENTER
    }

    public enum PositionY {
        TOP, BOTTOM, CENTER
    }
}