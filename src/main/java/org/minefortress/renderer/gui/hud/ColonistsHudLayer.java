package org.minefortress.renderer.gui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.HungerConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.fortress.resources.gui.craft.MissingCraftsmanScreen;
import org.minefortress.fortress.resources.gui.smelt.MissingBlacksmithScreen;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.c2s.ServerboundOpenCraftingScreenPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.professions.Profession;
import org.minefortress.renderer.gui.ColonistsScreen;
import org.minefortress.renderer.gui.professions.ProfessionsScreen;
import org.minefortress.renderer.gui.widget.FortressItemButtonWidget;
import org.minefortress.utils.ModUtils;

import java.util.Optional;

public class ColonistsHudLayer extends AbstractHudLayer {

    private final ButtonWidget manageColonistsButton;
    private final FortressItemButtonWidget professionsButton;
    private final FortressItemButtonWidget inventoryButton;
    private final FortressItemButtonWidget craftingButton;
    private final FortressItemButtonWidget furnaceButton;

    private int colonistsCount = 0;
    private boolean hovered;


    protected ColonistsHudLayer(MinecraftClient client, ItemRenderer itemRenderer) {
        super(client, itemRenderer);
        this.manageColonistsButton = new ButtonWidget(
                0,
                0,
                20,
                20,
                new LiteralText(""),
                btn -> client.setScreen(new ColonistsScreen()),
                "Manage pawns"
        );
        this.professionsButton = new FortressItemButtonWidget(
                Items.PLAYER_HEAD,
                btn -> client.setScreen(new ProfessionsScreen(getFortressClient())),
                "Manage professions"
        );
        this.inventoryButton = new FortressItemButtonWidget(
                Items.CHEST,
                btn -> client.setScreen(new CreativeInventoryScreen(client.player)),
                "Inventory"
        );
        this.craftingButton = new FortressItemButtonWidget(
                Items.CRAFTING_TABLE,
                btn -> {
                    if(hasProfessionInAVillage("crafter"))
                        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_OPEN_CRAFTING_TABLE, new ServerboundOpenCraftingScreenPacket(ServerboundOpenCraftingScreenPacket.ScreenType.CRAFTING));
                    else
                        this.client.setScreen(new MissingCraftsmanScreen());
                },
                "Crafting"
        );
        this.furnaceButton = new FortressItemButtonWidget(
                Items.FURNACE,
                btn -> {
                    if(hasProfessionInAVillage("blacksmith"))
                        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_OPEN_CRAFTING_TABLE, new ServerboundOpenCraftingScreenPacket(ServerboundOpenCraftingScreenPacket.ScreenType.FURNACE));
                    else
                        this.client.setScreen(new MissingBlacksmithScreen());
                },
                "Furnace"
        );
    }

    @Override
    void tick() {
    }

    @Override
    void render(MatrixStack matrices, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        if(ModUtils.getFortressClientManager().notInitialized()) return;

        final boolean colonsitsCountHovered = renderColonistsCount(matrices, font, screenWidth, screenHeight, mouseX, mouseY, delta);

        renderSelectedColonistInfo(matrices, font, screenHeight, fortressManager);

        this.hovered = colonsitsCountHovered;

        if(isCombat()) return;
        this.professionsButton.setPos(screenWidth / 2 - 91 + 35, screenHeight - 43);
        this.professionsButton.render(matrices, (int)mouseX, (int)mouseY, delta);

        this.inventoryButton.setPos(screenWidth / 2 - 91 + 35 + 20, screenHeight - 43);
        this.inventoryButton.render(matrices, (int)mouseX, (int)mouseY, delta);


        if(isSurvival()) {
            this.craftingButton.setPos(screenWidth / 2 - 91 + 35 + 40, screenHeight - 43);
            this.craftingButton.render(matrices, (int)mouseX, (int)mouseY, delta);

            this.furnaceButton.setPos(screenWidth / 2 - 91 + 35 + 60, screenHeight - 43);
            this.furnaceButton.render(matrices, (int)mouseX, (int)mouseY, delta);
        }
    }

    private void renderSelectedColonistInfo(MatrixStack matrices, TextRenderer font, int screenHeight, FortressClientManager fortressManager) {
        if(fortressManager.isSelectingColonist()){
            final Colonist selectedColonist = fortressManager.getSelectedColonist();

            final int colonistWinX = 0;
            final int colonistWinY = screenHeight - 85;
            int width = 120;
            final int height = 85;
            DrawableHelper.fillGradient(matrices, colonistWinX, colonistWinY, colonistWinX + width, colonistWinY + height, 0xc0101010, 0xd0101010, 100);

            final String name = Optional.ofNullable(selectedColonist.getCustomName()).map(Text::asString).orElse("");
            final String healthString = String.format("%.0f/%.0f", selectedColonist.getHealth(), selectedColonist.getMaxHealth());
            final String hungerString = String.format("%d/%d", selectedColonist.getCurrentFoodLevel(), HungerConstants.FULL_FOOD_LEVEL);
            final String professionId = selectedColonist.getProfessionId();
            final String professionName = Optional.ofNullable(fortressManager.getProfessionManager().getProfession(professionId)).map(Profession::getTitle).orElse("");
            final String task = selectedColonist.getCurrentTaskDesc();

            width = Math.max(width, font.getWidth(healthString));
            width = Math.max(width, font.getWidth(hungerString));
            width = Math.max(width, font.getWidth(professionName));
            width = Math.max(width, font.getWidth(task));

            Screen.drawCenteredText(matrices, font, name, colonistWinX + width / 2, colonistWinY + 5, 0xFFFFFF);

            int heartIconX = colonistWinX + 5;
            int heartIconY = colonistWinY + textRenderer.fontHeight + 10;
            renderIcon(matrices, heartIconX, heartIconY, 8, 8, 52, 0);
            textRenderer.draw(matrices, healthString, heartIconX + 10, heartIconY + 2, 0xFFFFFF);

            int hungerIconX = colonistWinX + width/2 + 5;
            renderIcon(matrices, hungerIconX, heartIconY, 8, 8, 52, 28);
            textRenderer.draw(matrices, hungerString, hungerIconX + 10, heartIconY + 2, 0xFFFFFF);

            textRenderer.draw(matrices, "Profession:", colonistWinX + 5, heartIconY + textRenderer.fontHeight + 5, 0xFFFFFF);
            textRenderer.draw(matrices, professionName, colonistWinX + 5, heartIconY + 2 * textRenderer.fontHeight + 5 , 0xFFFFFF);

            textRenderer.draw(matrices, "Task:", colonistWinX + 5, heartIconY + 3 * textRenderer.fontHeight + 10, 0xFFFFFF);
            textRenderer.draw(matrices, task, colonistWinX + 5, heartIconY + 4 * textRenderer.fontHeight + 10, 0xFFFFFF);
        }
    }

    private void renderIcon(MatrixStack matrices, int iconX, int iconY, int iconWidth, int iconHeight, int heartIconU, int heartIconV) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
        DrawableHelper.drawTexture(matrices, iconX, iconY, 110, heartIconU, heartIconV, iconWidth, iconHeight, 256, 256);
    }

    private boolean renderColonistsCount(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight, double mouseX, double mouseY, float delta) {
        final String colonistsCountString = "x" + colonistsCount;

        final int iconX = screenWidth / 2 - 91;
        final int iconY = screenHeight - 40;
        final float textX = screenWidth / 2f - 91 + 15;
        final int textY = screenHeight - 43;

        final int boundRightX = (int)textX + font.getWidth(colonistsCountString);
        final int boundBottomY = iconY + 20;

        final boolean hovered = mouseX >= iconX && mouseX <= boundRightX && mouseY >= iconY && mouseY < boundBottomY;

        super.itemRenderer.renderGuiItemIcon(new ItemStack(Items.PLAYER_HEAD), iconX, iconY);

        this.manageColonistsButton.x = (int)textX;
        this.manageColonistsButton.y = textY;
        this.manageColonistsButton.setMessage(new LiteralText(colonistsCountString));
        this.manageColonistsButton.render(p, (int)mouseX, (int)mouseY, delta);

//        if(hovered) {
//            super.renderTooltip(p, Text.of("Your Pawns count"), (int) mouseX, (int) mouseY);
//        }

        return hovered;
    }


    @Override
    boolean isHovered() {
        return this.hovered ||
                this.professionsButton.isHovered() ||
                this.inventoryButton.isHovered() ||
                this.craftingButton.isHovered() ||
                this.furnaceButton.isHovered() ||
                this.manageColonistsButton.isHovered();
    }

    @Override
    void onClick(double mouseX, double mouseY) {
        if(this.professionsButton.isHovered()) {
            this.professionsButton.onClick(mouseX, mouseY);
        }
        if (this.inventoryButton.isHovered()) {
            this.inventoryButton.onClick(mouseX, mouseY);
        }
        if (this.craftingButton.isHovered()) {
            this.craftingButton.onClick(mouseX, mouseY);
        }
        if (this.furnaceButton.isHovered()) {
            this.furnaceButton.onClick(mouseX, mouseY);
        }
        if(this.manageColonistsButton.isHovered()) {
            this.manageColonistsButton.onClick(mouseX, mouseY);
        }
    }

    private boolean hasProfessionInAVillage(String crafter) {

        return ModUtils.isClientInFortressGamemode() && ModUtils.getProfessionManager().hasProfession(crafter);
    }

}
