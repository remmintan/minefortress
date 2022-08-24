package org.minefortress.renderer.gui.blueprints;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.minefortress.blueprints.manager.BlueprintMetadata;
import org.minefortress.blueprints.renderer.BlueprintRenderer;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.fortress.resources.ItemInfo;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.ServerboundEditBlueprintPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.blueprints.handler.BlueprintScreenHandler;
import org.minefortress.renderer.gui.blueprints.handler.BlueprintSlot;

import java.util.List;

public final class BlueprintsScreen extends Screen {

    private static final Identifier INVENTORY_TABS_TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
    private static final String BACKGROUND_TEXTURE = "textures/gui/container/creative_inventory/tab_items.png";
    private static final Identifier BLUEPRINT_PREVIEW_BACKGROUND_TEXTURE = new Identifier("textures/gui/recipe_book.png");
    private static final LiteralText EDIT_BLUEPRINT_TEXT = new LiteralText("right click to edit");
    private static final LiteralText ADD_BLUEPRINT_TEXT = new LiteralText("click to add blueprint");

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

            if(client instanceof FortressMinecraftClient fortressClient && fortressClient.isFortressGamemode()) {
                super.init();
                this.x = (this.width - backgroundWidth - previewWidth - previewOffset) / 2;
                this.y = (this.height - backgroundHeight) / 2;

                this.handler = new BlueprintScreenHandler(this.client);
                this.blueprintRenderer = fortressClient.getBlueprintRenderer();
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
                if(this.handler.getFocusedSlot() == BlueprintSlot.EMPTY) {
                    if(this.client != null)
                    this.client.setScreen(new AddBlueprintScreen(handler.getSelectedGroup()));
                    return true;
                }

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
        for (int i = 0; i < 45; i++) {
            int slotColumn = i % 9;
            int slotRow = i / 9;

            int slotX = slotColumn * 18 + 9;
            int slotY = slotRow * 18 + 18;

            final BlueprintSlot blueprintSlot = i<currentSlotsSize ? currentSlots.get(i) : BlueprintSlot.EMPTY;
            this.drawSlot(blueprintSlot, slotColumn, slotRow);

            if (!this.isPointOverSlot(slotX, slotY, mouseX, mouseY)) continue;
            final FortressClientManager fortressClientManager = getFortressClientManager();
            final var resourceManager = fortressClientManager.getResourceManager();
            this.handler.focusOnSlot(blueprintSlot);
            HandledScreen.drawSlotHighlight(matrices, slotX, slotY, this.getZOffset());

            if(blueprintSlot != BlueprintSlot.EMPTY) {
                if(fortressClientManager.isSurvival()) {
                    final var stacks = blueprintSlot.getBlockData().getStacks();
                    for (int i1 = 0; i1 < stacks.size(); i1++) {
                        final ItemInfo stack = stacks.get(i1);
                        final var hasItem = resourceManager.hasItem(stack, stacks);
                        final var itemX = this.x - this.backgroundWidth/2 + 25 + i1%10 * 30;
                        final var itemY = i1/10 * 20 + this.backgroundHeight;
                        final var convertedItem = convertItemIconInTheGUI(stack);
                        itemRenderer.renderInGui(new ItemStack(convertedItem), itemX, itemY);
                        this.textRenderer.draw(matrices, String.valueOf(stack.amount()), itemX + 17, itemY + 7, hasItem?0xFFFFFF:0xFF0000);
                    }
                }

                this.blueprintRenderer.renderBlueprintPreview(blueprintSlot.getMetadata().getFile(), BlockRotation.NONE);
            }
        }

        this.drawForeground(matrices);
        if(this.handler.hasFocusedSlot()){
            final var editText = handler.getFocusedSlot() != BlueprintSlot.EMPTY ? EDIT_BLUEPRINT_TEXT : ADD_BLUEPRINT_TEXT;
            this.textRenderer.draw(
                    matrices,
                    editText,
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

    private Item convertItemIconInTheGUI(ItemInfo stack) {
        final var originalItem = stack.item();
        if(Items.FARMLAND.equals(originalItem)) {
            return Items.DIRT;
        }
        return originalItem;
    }

    private FortressClientManager getFortressClientManager() {
        final var fortressClient = (FortressMinecraftClient) this.client;
        return fortressClient.getFortressClientManager();
    }

    @Override
    public boolean shouldPause() {
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
        final float scaleParameter = 2f;

        RenderSystem.enableDepthTest();
        int slotX = slotColumn * 18 + 9 + 5;
        int slotY = slotRow * 18 + 18 + 5;
        if(slot == BlueprintSlot.EMPTY){
            final var brickStack = new ItemStack(Items.BRICK);
            this.itemRenderer.renderInGui(brickStack, (int)(slotX*scaleParameter), (int)(slotY*scaleParameter));
        } else {
            final BlueprintMetadata metadata = slot.getMetadata();
            final var enoughResources = !getFortressClientManager().isSurvival() || slot.isEnoughResources();
            if(this.client != null){
                this.blueprintRenderer.renderBlueprintInGui(metadata.getFile(), BlockRotation.NONE, slotColumn, slotRow, enoughResources);
            }

            if(client instanceof FortressMinecraftClient fortressClient){
                if(metadata.isPremium() && !fortressClient.isSupporter()){
                    final MatrixStack matrices = RenderSystem.getModelViewStack();
                    matrices.push();
                    matrices.scale(1/scaleParameter, 1/scaleParameter, 1/scaleParameter);
                    RenderSystem.applyModelViewMatrix();

                    this.itemRenderer.renderInGui(new ItemStack(Items.GOLD_INGOT), (int)(slotX*scaleParameter), (int)(slotY*scaleParameter));
                    matrices.pop();
                    RenderSystem.applyModelViewMatrix();
                }
            }
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
        if (this.handler.hasFocusedSlot() && this.handler.getFocusedSlot() != BlueprintSlot.EMPTY) {
            this.renderTooltip(matrices, this.handler.getFocusedSlotName(), x, y);
        }
    }

}
