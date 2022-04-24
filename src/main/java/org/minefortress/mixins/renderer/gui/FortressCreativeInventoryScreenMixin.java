package org.minefortress.mixins.renderer.gui;

import com.chocohead.mm.api.ClassTinkerers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchableContainer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.fortress.resources.client.ClientResourceManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.renderer.gui.resources.FortressSurvivalInventoryScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.Map;

@Mixin(CreativeInventoryScreen.class)
public abstract class FortressCreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {

    @Shadow @Final private static Identifier TEXTURE;
    @Shadow @Final private static String TAB_TEXTURE_PREFIX;
    @Shadow private static int selectedTab;

    @Shadow protected abstract void renderTabIcon(MatrixStack matrices, ItemGroup group);

    @Shadow private TextFieldWidget searchBox;
    @Shadow private float scrollPosition;

    @Shadow protected abstract boolean hasScrollbar();

    @Shadow private @Nullable Slot deleteItemSlot;
    @Shadow @Final private static Text DELETE_ITEM_SLOT_TEXT;
    @Shadow private boolean scrolling;
    @Shadow @Final private Map<Identifier, Tag<Item>> searchResultTags;

    @Shadow protected abstract void searchForTags(String id2);

    @Shadow protected abstract boolean isClickInTab(ItemGroup group, double mouseX, double mouseY);

    @Shadow protected abstract void setSelectedTab(ItemGroup group);

    @Shadow protected abstract boolean renderTabTooltipIfHovered(MatrixStack matrices, ItemGroup group, int mouseX, int mouseY);

    private static final GameMode FORTRESS_GAMEMODE = ClassTinkerers.getEnum(GameMode.class, "FORTRESS");

    public FortressCreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void init(PlayerEntity player, CallbackInfo ci) {
        if(isFortressGamemode() && isNotCreative()){
            super.handler = new FortressSurvivalInventoryScreenHandler(player);
            player.currentScreenHandler = super.handler;
        }
    }

    private boolean isNotCreative() {
        return !getClientManager().isCreative();
    }

    @Redirect(method = "onMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;"))
    ItemEntity dropItem(ClientPlayerEntity instance, ItemStack itemStack, boolean b) {
        if(isFortressGamemode())
            return null;
        else {
            return instance.dropItem(itemStack, b);
        }
    }

    @Redirect(method = "onMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;dropCreativeStack(Lnet/minecraft/item/ItemStack;)V"))
    void dropCreativeStack(ClientPlayerInteractionManager instance, ItemStack stack) {
        if(!isFortressGamemode()) {
            instance.dropCreativeStack(stack);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if(isFortressGamemode() && isNotCreative()) {
            if (button == 0) {
                double d = mouseX - (double)this.x;
                double e = mouseY - (double)this.y;
                this.scrolling = false;
                for (ItemGroup itemGroup : getResourceManager().getGroups()) {
                    if (!this.isClickInTab(itemGroup, d, e)) continue;
                    this.setSelectedTab(itemGroup);
                    cir.setReturnValue(true);
                }
            }
            cir.setReturnValue(super.mouseReleased(mouseX, mouseY, button));
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(isFortressGamemode() && isNotCreative()) {
            this.renderBackground(matrices);
            super.render(matrices, mouseX, mouseY, delta);
            for (ItemGroup itemGroup : getResourceManager().getGroups()) {
                if (this.renderTabTooltipIfHovered(matrices, itemGroup, mouseX, mouseY)) break;
            }
            if (this.deleteItemSlot != null && selectedTab == ItemGroup.INVENTORY.getIndex() && this.isPointWithinBounds(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, mouseX, mouseY)) {
                this.renderTooltip(matrices, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            this.drawMouseoverTooltip(matrices, mouseX, mouseY);

            ci.cancel();
        }
    }


    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    public void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if(isFortressGamemode() && isNotCreative()) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            ItemGroup itemGroup = ItemGroup.GROUPS[selectedTab];
            for (ItemGroup itemGroup2 : getResourceManager().getGroups()) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, TEXTURE);
                if (itemGroup2.getIndex() == selectedTab) continue;
                this.renderTabIcon(matrices, itemGroup2);
            }
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, new Identifier(TAB_TEXTURE_PREFIX + itemGroup.getTexture()));
            this.drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
            this.searchBox.render(matrices, mouseX, mouseY, delta);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            int i = this.x + 175;
            int j = this.y + 18;
            int k = j + 112;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            if (itemGroup.hasScrollbar()) {
                this.drawTexture(matrices, i, j + (int)((float)(k - j - 17) * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
            }
            this.renderTabIcon(matrices, itemGroup);
            if (itemGroup == ItemGroup.INVENTORY) {
                InventoryScreen.drawEntity(this.x + 88, this.y + 45, 20, this.x + 88 - mouseX, this.y + 45 - 30 - mouseY, this.client.player);
            }

            ci.cancel();
        }
    }

    @Inject(method = "setSelectedTab", at = @At("HEAD"), cancellable = true)
    public void setSelectedTabInj(ItemGroup group, CallbackInfo ci) {
        if(isFortressGamemode() && isNotCreative()){
            selectedTab = group.getIndex();
            this.cursorDragSlots.clear();

            this.handler.itemList.clear();

            final var stacks = getResourceManager().getStacks(group);
            this.handler.itemList.addAll(stacks);

            this.scrollPosition = 0.0f;
            this.handler.scrollItems(0);

            ci.cancel();
        }
    }

    @Inject(method = "search", at = @At("HEAD"), cancellable = true)
    public void search(CallbackInfo ci) {
        if(isFortressGamemode() && isNotCreative()) {
            this.searchResultTags.clear();
            String string = this.searchBox.getText();
            if (string.isEmpty()) {
                for (Item item : Registry.ITEM) {
                    item.appendStacks(ItemGroup.SEARCH, this.handler.itemList);
                }
            } else {
                SearchableContainer<ItemStack> searchable;
                if (string.startsWith("#")) {
                    string = string.substring(1);
                    searchable = this.client.getSearchableContainer(SearchManager.ITEM_TAG);
                    this.searchForTags(string);
                } else {
                    searchable = this.client.getSearchableContainer(SearchManager.ITEM_TOOLTIP);
                }
                this.handler.itemList.addAll(searchable.findAll(string.toLowerCase(Locale.ROOT)));
            }
            this.scrollPosition = 0.0f;
            this.handler.scrollItems(0.0f);

            ci.cancel();
        }
    }

    private boolean isFortressGamemode() {
        return getClient().interactionManager != null && FORTRESS_GAMEMODE == getClient().interactionManager.getCurrentGameMode();
    }

    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    private FortressClientManager getClientManager() {
        return ((FortressMinecraftClient) getClient()).getFortressClientManager();
    }

    private ClientResourceManager getResourceManager() {
        return getClientManager().getResourceManager();
    }

}
