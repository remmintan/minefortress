package org.minefortress.renderer.gui.blueprints;

import com.chocohead.mm.api.ClassTinkerers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.minefortress.blueprints.manager.BlueprintMetadata;
import org.minefortress.blueprints.renderer.BlueprintRenderer;
import org.minefortress.interfaces.FortressClientWorld;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.blueprints.handler.BlueprintScreenHandler;
import org.minefortress.renderer.gui.blueprints.handler.BlueprintSlot;

import java.util.List;

public final class BlueprintsScreen extends Screen {

    private static final Identifier INVENTORY_TABS_TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private static final String BACKGROUND_TEXTURE = "textures/gui/container/creative_inventory/tab_items.png";
    private static final Identifier BLUEPRINT_PREVIEW_BACKGROUND_TEXTURE = new Identifier("textures/gui/recipe_book.png");
    private static final LiteralText EDIT_BLUEPRINT_TEXT = new LiteralText("right click to edit");
    private static final LiteralText ONLY_PATRON_EDIT_TEXT = new LiteralText("only patrons can edit this blueprint");

    private final int backgroundWidth = 195;
    private final int backgroundHeight = 136;

    private final int previewWidth = 120;
    private final int previewOffset = 4;

    private BlueprintRenderer blueprintRenderer;

    private int x;
    private int y;

    private boolean isScrolling = false;
    private float scrollPosition = 0;

    private BlueprintScreenHandler handler;

    public BlueprintsScreen() {
        super(new LiteralText("Blueprints"));
    }

    @Override
    protected void init() {
        if(this.client != null) {
            this.client.keyboard.setRepeatEvents(true);
            final ClientPlayerInteractionManager interactionManager = this.client.interactionManager;
            if(interactionManager != null && interactionManager.getCurrentGameMode() == ClassTinkerers.getEnum(GameMode.class, "FORTRESS")) {
                super.init();
                this.x = (this.width - backgroundWidth - previewWidth - previewOffset) / 2;
                this.y = (this.height - backgroundHeight) / 2;

                this.handler = new BlueprintScreenHandler(this.client);
                this.blueprintRenderer = ((FortressMinecraftClient)this.client).getBlueprintRenderer();
            } else {
                this.client.setScreen(null);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button == 0) {
            for(BlueprintGroup group : BlueprintGroup.values()) {
                if(isClickInTab(group, mouseX, mouseY)) return true;
            }

            if(this.isClickInScrollbar(mouseX, mouseY)) {
                this.isScrolling = this.handler.isNeedScrollbar();
                return true;
            }
        }

        if(super.mouseClicked(mouseX, mouseY, button)) return true;

        if(button == 0 || button == 1) {
            if(this.handler.hasFocusedSlot()) {
                if(button == 1) return true;

                if(this.client != null){
                    this.client.setScreen(null);
                }
                this.handler.clickOnFocusedSlot();
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isScrolling) {
            int minScroll = this.y + 18;
            int maxScroll = minScroll + 112;
            this.scrollPosition = ((float)mouseY - (float)minScroll - 7.5f) / ((float)(maxScroll - minScroll) - 15.0f);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
            this.handler.scroll(scrollPosition);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isScrolling = false;
            for (BlueprintGroup blueprintGroup: BlueprintGroup.values()) {
                if (this.isClickInTab(blueprintGroup, mouseX, mouseY)) {
                    this.handler.selectGroup(blueprintGroup);
                    this.scrollPosition = 0;
                    return true;
                }
            }
        }

        if(button == 1) {
            if(this.handler.hasFocusedSlot()) {
                if(client != null) {
                    this.client.setScreen(null);
                }
                this.handler.sendEditPacket(this);
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!this.handler.isNeedScrollbar()) {
            return false;
        }
        int i = (this.handler.getSelectedGroupSize() + 9 - 1) / 9 - 5;
        this.scrollPosition = (float)((double)this.scrollPosition - amount / (double)i);
        this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
        this.handler.scroll(scrollPosition);
        return true;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBackground(matrices, delta, mouseX, mouseY);
        RenderSystem.disableDepthTest();
        super.render(matrices, mouseX, mouseY, delta);

        int screenX = this.x;
        int screenY = this.y;

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(screenX, screenY, 0.0);
        RenderSystem.applyModelViewMatrix();
        this.handler.focusOnSlot(null);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        final List<BlueprintSlot> currentSlots = this.handler.getCurrentSlots();
        final int currentSlotsSize = currentSlots.size();
        for (int i = 0; i < currentSlotsSize; i++) {
            int slotColumn = i % 9;
            int slotRow = i / 9;

            int slotX = slotColumn * 18 + 9;
            int slotY = slotRow * 18 + 18;

            final BlueprintSlot blueprintSlot = currentSlots.get(i);
            this.drawSlot(blueprintSlot, slotColumn, slotRow);

            if (!this.isPointOverSlot(slotX, slotY, mouseX, mouseY)) continue;
            this.handler.focusOnSlot(blueprintSlot);
            HandledScreen.drawSlotHighlight(matrices, slotX, slotY, this.getZOffset());

            this.blueprintRenderer.renderBlueprintPreview(blueprintSlot.getMetadata().getFile(), BlockRotation.NONE);
        }

        this.drawForeground(matrices);
        if(this.handler.hasFocusedSlot()){
            final BlueprintMetadata focusedSlotMetadata = this.handler.getFocusedSlotMetadata();
            final FortressMinecraftClient fortressClient = (FortressMinecraftClient) this.client;
            this.textRenderer.draw(
                    matrices,
                    (fortressClient!=null && fortressClient.isSupporter()) || !focusedSlotMetadata.isPremium() ? EDIT_BLUEPRINT_TEXT : ONLY_PATRON_EDIT_TEXT,
                    this.backgroundWidth + this.previewOffset + 3,
                    this.backgroundHeight - this.textRenderer.fontHeight - 3,
                    0xFFFFFF
            );
        }

        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();

        for(BlueprintGroup group : BlueprintGroup.values()) {
            this.renderTabTooltipIfHovered(matrices, group, mouseX, mouseY);
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        if(this.client!=null)
            this.client.keyboard.setRepeatEvents(false);
    }

    private boolean isClickInTab(BlueprintGroup group, double mouseX, double mouseY) {
        mouseX -= this.x;
        mouseY -= this.y;

        int columnNumber = group.ordinal();
        int x = 28 * columnNumber;
        int y = 0;
        if (columnNumber > 0) x += columnNumber;

        if (group.isTopRow()) {
            y -= 32;
        } else {
            y += this.backgroundHeight;
        }
        return mouseX >= (double)x && mouseX <= (double)(x + 28) && mouseY >= (double)y && mouseY <= (double)(y + 32);
    }

    private boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.x;
        int j = this.y;
        int k = i + 175;
        int l = j + 18;
        int m = k + 14;
        int n = l + 112;
        return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)m && mouseY < (double)n;
    }

    private boolean isPointOverSlot(int slotX, int slotY, int mouseX, int mouseY) {
        int screenX = this.x;
        int screenY = this.y;

        return mouseX >= screenX + slotX && mouseX < screenX + slotX + 18 && mouseY >= screenY + slotY && mouseY < screenY + slotY + 18;
    }



    private void drawForeground(MatrixStack matrices) {
        final BlueprintGroup selectedGroup = this.handler.getSelectedGroup();
        if (selectedGroup != null) {
            RenderSystem.disableBlend();
            this.textRenderer.draw(matrices, selectedGroup.getNameText(), 8.0f, 6.0f, 0x404040);
        }
    }

    private void drawSlot(BlueprintSlot slot, int slotColumn, int slotRow) {
        this.setZOffset(100);
        this.itemRenderer.zOffset = 100.0f;

        RenderSystem.enableDepthTest();
        if(this.client != null){
            final BlueprintMetadata metadata = slot.getMetadata();
            this.blueprintRenderer.renderBlueprintInGui(metadata.getFile(), BlockRotation.NONE, slotColumn, slotRow);
        }

        this.itemRenderer.zOffset = 0.0f;
        this.setZOffset(0);
    }



    private void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        final BlueprintGroup selectedGroup = this.handler.getSelectedGroup();
        for (BlueprintGroup bg : BlueprintGroup.values()) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, INVENTORY_TABS_TEXTURE);

            if (selectedGroup == bg) continue;
            this.renderTabIcon(matrices, bg);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new Identifier(BACKGROUND_TEXTURE));
        this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
//        this.searchBox.render(matrices, mouseX, mouseY, delta);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int i = this.x + 175;
        int j = this.y + 18;
        int k = j + 112;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, INVENTORY_TABS_TEXTURE);

        this.drawTexture(matrices, i, j + (int)((float)(k - j - 17) * this.scrollPosition), 232 + (this.handler.isNeedScrollbar() ? 0 : 12), 0, 12, 15);

        if(selectedGroup != null)
            this.renderTabIcon(matrices, selectedGroup);


        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BLUEPRINT_PREVIEW_BACKGROUND_TEXTURE);

        this.drawTexture(matrices, this.x + this.backgroundWidth + this.previewOffset, this.y, 15, 23, this.previewWidth, this.backgroundHeight);

    }


    private void renderTabIcon(MatrixStack matrices, BlueprintGroup group) {
        boolean isSelectedGroup = group == this.handler.getSelectedGroup();
        int columnNumber = group.ordinal() % 7;
        int texX = columnNumber * 28;
        int texY = 0;
        int x = this.x + 28 * columnNumber;
        int y = this.y;
        if (columnNumber > 0) {
            x += columnNumber;
        }
        boolean topRow = group.isTopRow();
        if (topRow) {
            y -= 28;
        } else {
            texY += 64;
            y += this.backgroundHeight - 4;
        }
        if (isSelectedGroup) {
            texY += 32;
        }
        this.drawTexture(matrices, x, y, texX, texY, 28, 32);
        this.itemRenderer.zOffset = 0.0f;
        int yIconDelta = topRow ? 1 : -1;
        ItemStack icon = group.getIcon();
        this.itemRenderer.renderInGuiWithOverrides(icon, x += 6, y += 8 + yIconDelta);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, icon, x, y);
        this.itemRenderer.zOffset = 0.0f;
    }

    private void renderTabTooltipIfHovered(MatrixStack matrices, BlueprintGroup group, int mouseX, int mouseY) {
        int columnNumber = group.ordinal();
        int x = 28 * columnNumber + this.x;
        int y = this.y;
        if (columnNumber > 0) {
            x += columnNumber;
        }
        if (group.isTopRow()) {
            y -= 32;
        } else {
            y += this.backgroundHeight;
        }
        if (this.isPointWithinBounds(x + 3, y + 3, 23, 27, mouseX, mouseY)) {
            this.renderTooltip(matrices, group.getNameText(), mouseX, mouseY);

        }
    }

    private boolean isPointWithinBounds(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
        if (this.handler.hasFocusedSlot()) {
            this.renderTooltip(matrices, this.handler.getFocusedSlotName(), x, y);
        }
    }

}
