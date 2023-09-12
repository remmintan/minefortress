package org.minefortress.mixins.renderer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchProvider;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.minefortress.fortress.resources.client.ClientResourceManager;
import org.minefortress.renderer.gui.resources.FortressSurvivalInventoryScreenHandler;
import org.minefortress.utils.ModUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.Set;

@Mixin(CreativeInventoryScreen.class)
public abstract class FortressCreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {

    @Shadow @Final private static Identifier TEXTURE;
    @Shadow @Final private static String TAB_TEXTURE_PREFIX;
    @Shadow private static ItemGroup selectedTab = ItemGroups.getDefaultTab();
    @Shadow private TextFieldWidget searchBox;
    @Shadow private float scrollPosition;

    @Shadow protected abstract boolean hasScrollbar();

    @Shadow private @Nullable Slot deleteItemSlot;
    @Shadow @Final private static Text DELETE_ITEM_SLOT_TEXT;
    @Shadow private boolean scrolling;
    @Shadow @Final private Set<TagKey<Item>> searchResultTags;

    @Shadow protected abstract void searchForTags(String id2);

    @Shadow protected abstract boolean isClickInTab(ItemGroup group, double mouseX, double mouseY);

    @Shadow protected abstract void setSelectedTab(ItemGroup group);
    @Shadow @Final static SimpleInventory INVENTORY;

    @Shadow public abstract boolean isInventoryTabSelected();

    @Shadow protected abstract void renderTabIcon(DrawContext context, ItemGroup group);

    @Shadow protected abstract boolean renderTabTooltipIfHovered(DrawContext context, ItemGroup group, int mouseX, int mouseY);

    public FortressCreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void init(PlayerEntity player, FeatureSet enabledFeatures, boolean operatorTabEnabled, CallbackInfo ci) {
        if(isFortressSurvival()){
            super.handler = new FortressSurvivalInventoryScreenHandler(player, INVENTORY);
            player.currentScreenHandler = super.handler;
        }
    }



    @Redirect(method = "onMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;"))
    ItemEntity dropItem(ClientPlayerEntity instance, ItemStack itemStack, boolean b) {
        if(ModUtils.isClientInFortressGamemode())
            return null;
        else {
            return instance.dropItem(itemStack, b);
        }
    }

    @Inject(method = "onMouseClick", at = @At("HEAD"), cancellable = true)
    void onCLickHead(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if(ModUtils.isFortressGamemode(getClient().player) && actionType == SlotActionType.SWAP) {
            ci.cancel();
        }
    }

    @Redirect(method = "onMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;dropCreativeStack(Lnet/minecraft/item/ItemStack;)V"))
    void dropCreativeStack(ClientPlayerInteractionManager instance, ItemStack stack) {
        if(!ModUtils.isClientInFortressGamemode()) {
            instance.dropCreativeStack(stack);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if(isFortressSurvival()) {
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(isFortressSurvival()) {
            this.renderBackground(context);
            super.render(context, mouseX, mouseY, delta);
            for (ItemGroup itemGroup : getResourceManager().getGroups()) {
                if (this.renderTabTooltipIfHovered(context, itemGroup, mouseX, mouseY)) break;
            }
            if (this.deleteItemSlot != null && selectedTab.getType() == ItemGroup.Type.INVENTORY && this.isPointWithinBounds(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, mouseX, mouseY)) {
                context.drawTooltip(this.textRenderer, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            this.drawMouseoverTooltip(context, mouseX, mouseY);

            ci.cancel();
        }
    }


    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if(isFortressSurvival()) {
            ItemGroup itemGroup = selectedTab;
            for (ItemGroup itemGroup2 : getResourceManager().getGroups()) {
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderTexture(0, TEXTURE);
                if (itemGroup2 == selectedTab) continue;
                this.renderTabIcon(context, itemGroup2);
            }
            final var texture = new Identifier(TAB_TEXTURE_PREFIX + itemGroup.getTexture());
            context.drawTexture(texture, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
            this.searchBox.render(context, mouseX, mouseY, delta);
            int i = this.x + 175;
            int j = this.y + 18;
            int k = j + 112;
            if (itemGroup.hasScrollbar()) {
                context.drawTexture(TEXTURE, i, j + (int)((float)(k - j - 17) * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
            }
            this.renderTabIcon(context, itemGroup);
            if (itemGroup == Registries.ITEM_GROUP.get(ItemGroups.INVENTORY)) {
                InventoryScreen.drawEntity(context, this.x + 88, this.y + 45, 20, this.x + 88 - mouseX, this.y + 45 - 30 - mouseY, ModUtils.getClientPlayer());
            }

            ci.cancel();
        }
    }

    @Inject(method = "handledScreenTick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        if(ModUtils.isClientInFortressGamemode() && ModUtils.getFortressClientManager().isSurvival()) {
            if(this.isInventoryTabSelected()) {
                this.setSelectedTab(Registries.ITEM_GROUP.get(ItemGroups.BUILDING_BLOCKS));
            }
            ci.cancel();
        }
    }

    @Inject(method = "setSelectedTab", at = @At("HEAD"), cancellable = true)
    public void setSelectedTabInj(ItemGroup group, CallbackInfo ci) {
        if(isFortressSurvival()){
            selectedTab = group;
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
        if(isFortressSurvival()) {
            this.searchResultTags.clear();
            String string = this.searchBox.getText();
            if (string.isEmpty()) {
                for (Item item : Registries.ITEM) {
                    this.handler.itemList.addAll(selectedTab.getDisplayStacks());
                }
            } else {
                final SearchProvider searchProvider;
                if (string.startsWith("#")) {
                    string = string.substring(1);
                    searchProvider = this.client.getSearchProvider(SearchManager.ITEM_TAG);
                    this.searchForTags(string);
                } else {
                    searchProvider = getClient().getSearchProvider(SearchManager.ITEM_TOOLTIP);
                }
                this.handler.itemList.addAll(searchProvider.findAll(string.toLowerCase(Locale.ROOT)));
            }
            this.scrollPosition = 0.0f;
            this.handler.scrollItems(0.0f);

            ci.cancel();
        }
    }

    @Unique
    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    @Unique
    private static boolean isFortressSurvival() {
        return ModUtils.isClientInFortressGamemode() && !ModUtils.getFortressClientManager().isCreative();
    }

    @Unique
    private static ClientResourceManager getResourceManager() {
        return ModUtils.getFortressClientManager().getResourceManager();
    }

}
