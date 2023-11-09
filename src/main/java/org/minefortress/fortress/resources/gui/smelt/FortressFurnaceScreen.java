package org.minefortress.fortress.resources.gui.smelt;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.recipebook.FurnaceRecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.ScreenType;
import net.remmintan.mods.minefortress.core.interfaces.client.IClientManagersProvider;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundOpenCraftingScreenPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.fortress.resources.gui.AbstractFortressRecipeScreen;
import org.minefortress.interfaces.IFortressMinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class FortressFurnaceScreen extends AbstractFortressRecipeScreen<FortressFurnaceScreenHandler> {


    private static final Identifier LIT_PROGRESS_TEXTURE = new Identifier("container/furnace/lit_progress");
    private static final Identifier BURN_PROGRESS_TEXTURE = new Identifier("container/furnace/burn_progress");
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/container/furnace.png");
    private final FurnaceRecipeBookScreen furnaceRecipeBookScreen = new FurnaceRecipeBookScreen();
    private final FortressFurnaceScreenHandler furnaceScreenHandler;

    private final List<AddedFurnace> addedFurnaces = new ArrayList<>();

    public FortressFurnaceScreen(FortressFurnaceScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.furnaceScreenHandler = handler;
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        final var furnaces = furnaceScreenHandler.getFurnaces();
        while (addedFurnaces.size() < furnaces.size()) {
            final var addedFurnaces = this.addedFurnaces.size();
            final var otherFurnace = furnaces.get(addedFurnaces);
            final var width = 100;

            final ButtonWidget.PressAction action = (buttonWidget) -> {
                final var posX = otherFurnace.getPosX();
                final var posY = otherFurnace.getPosY();
                final var posZ = otherFurnace.getPosZ();
                final var packet = new ServerboundOpenCraftingScreenPacket(
                        ScreenType.FURNACE,
                        new BlockPos(posX, posY, posZ)
                );
                if (this.client != null) this.close();
                FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_OPEN_CRAFTING_TABLE, packet);
            };

            final var btn = ButtonWidget
                    .builder(Text.literal("Fur. " + addedFurnaces), action)
                    .dimensions(
                            this.width - 5 - width,
                            5 + addedFurnaces * 25,
                            width,
                            20
                    )
                    .build();

            this.addDrawableChild(btn);
            this.addedFurnaces.add(new AddedFurnace(otherFurnace, btn));
        }

        for(AddedFurnace addedFurnace : addedFurnaces) {
            final var furnace = addedFurnace.furnace;
            final var btn = addedFurnace.button;

            final var selectedLabel = furnace.isSelected() ? "*" : "";
            final var isBurning = furnace.getBurnTime() > 0;
            final var burningLabel = isBurning ? (furnace.getCookProgress()+"%") : ("not burning");

            btn.setMessage(Text.literal("Fur."+selectedLabel+": "+burningLabel));
        }
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);

        int fuelProgress = (this.handler).getFuelProgress()+1;
        drawContext.drawGuiTexture(LIT_PROGRESS_TEXTURE, 14, 14, 0, 14 - fuelProgress, x + 56, y + 36 + 15 - fuelProgress, 14, fuelProgress);

        int cookProgress = (this.handler).getCookProgress();
        drawContext.drawGuiTexture(BURN_PROGRESS_TEXTURE, 24, 16, 0, 0, x + 79, y + 34, cookProgress, 16);
    }

    @Override
    public RecipeBookWidget getRecipeBookWidget() {
        return furnaceRecipeBookScreen;
    }

    @Override
    protected boolean professionRequirementSatisfied() {
        final var provider = getManagerProvider();
        final var clientManager = provider.get_ClientFortressManager();
        return getFortressClient().is_FortressGamemode() && clientManager.getProfessionManager().hasProfession("blacksmith");
    }

    private IClientManagersProvider getManagerProvider() {
        return (IClientManagersProvider) MinecraftClient.getInstance();
    }

    private IFortressMinecraftClient getFortressClient() {
        return (IFortressMinecraftClient) MinecraftClient.getInstance();
    }

    @Override
    protected void drawBackground(DrawContext drawContext, float delta, int mouseX, int mouseY) {
        int k;
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int i = this.x;
        int j = this.y;
        drawContext.drawTexture(BACKGROUND_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        if (this.handler.isBurning()) {
            k = this.handler.getFuelProgress();
            drawContext.drawTexture(BACKGROUND_TEXTURE, i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }
        k = this.handler.getCookProgress();
        drawContext.drawTexture(BACKGROUND_TEXTURE, i + 79, j + 34, 176, 14, k + 1, 16);
    }

    private record AddedFurnace(FortressFurnacePropertyDelegate furnace, ButtonWidget button) {}
}
