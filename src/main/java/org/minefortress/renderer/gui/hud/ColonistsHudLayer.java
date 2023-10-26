package org.minefortress.renderer.gui.hud;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.remmintan.mods.minefortress.core.ScreenType;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundOpenCraftingScreenPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.fortress.resources.gui.craft.MissingCraftsmanScreen;
import org.minefortress.fortress.resources.gui.smelt.MissingBlacksmithScreen;
import org.minefortress.renderer.gui.ColonistsScreen;
import org.minefortress.renderer.gui.professions.ProfessionsScreen;
import org.minefortress.renderer.gui.widget.DynamicTextButtonWidget;
import org.minefortress.renderer.gui.widget.ItemButtonWidget;
import org.minefortress.renderer.gui.widget.ItemHudElement;
import org.minefortress.utils.ModUtils;

public class ColonistsHudLayer extends AbstractHudLayer {

    private final ItemButtonWidget furnaceButton;
    private final ItemButtonWidget craftingButton;

    protected ColonistsHudLayer(MinecraftClient client) {
        super(client);
        this.addElement(
                new DynamicTextButtonWidget(
                        15,
                        0,
                        20,
                        20,
                        btn -> client.setScreen(new ColonistsScreen()),
                        "Manage pawns",
                        this::getColonistsCountText
                )
        );

        this.addElement(
            new ItemButtonWidget(
                    35, 0,
                Items.PLAYER_HEAD,
                btn -> client.setScreen(new ProfessionsScreen(CoreModUtils.getMineFortressManagersProvider())),
                "Manage professions"
            )
        );
        this.addElement(
                new ItemButtonWidget(
                        35+20, 0,
                        Items.CHEST,
                        btn -> client.setScreen(new CreativeInventoryScreen(client.player, FeatureSet.empty(), false)),
                        "Inventory"
                )
        );
        craftingButton = new ItemButtonWidget(
                35 + 40, 0,
                Items.CRAFTING_TABLE,
                btn -> {
                    if (hasProfessionInAVillage("crafter"))
                        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_OPEN_CRAFTING_TABLE, new ServerboundOpenCraftingScreenPacket(ScreenType.CRAFTING));
                    else
                        this.client.setScreen(new MissingCraftsmanScreen());
                },
                "Crafting"
        );
        furnaceButton = new ItemButtonWidget(
                35 + 60, 0,
                Items.FURNACE,
                btn -> {
                    if (hasProfessionInAVillage("blacksmith")){
                        if(ModUtils.getFortressClientManager().hasRequiredBlock(Blocks.FURNACE, true, 0)) {
                            FortressClientNetworkHelper.send(
                                    FortressChannelNames.FORTRESS_OPEN_CRAFTING_TABLE,
                                    new ServerboundOpenCraftingScreenPacket(ScreenType.FURNACE)
                            );
                        } else {
                            this.client.setScreen(new MissingBlacksmithScreen(true));
                        }
                    } else {
                        this.client.setScreen(new MissingBlacksmithScreen(false));
                    }
                },
                "Furnace"
        );
        this.addElement(craftingButton);
        this.addElement(furnaceButton);

        this.addElement(
                new ItemHudElement(
                        0, 3,
                        Items.PLAYER_HEAD
                )
        );

        this.setBasepoint(-91, -43, PositionX.CENTER, PositionY.BOTTOM);
    }

    private String getColonistsCountText() {
        return "x" + ModUtils.getFortressClientManager().getTotalColonistsCount();
    }

    private boolean hasProfessionInAVillage(String professionId) {
        return ModUtils.isClientInFortressGamemode() && ModUtils.getProfessionManager().hasProfession(professionId);
    }

    @Override
    public void tick() {
        super.tick();
        if(ModUtils.getFortressClientManager().isCreative()) {
            craftingButton.visible = false;
            furnaceButton.visible = false;
        } else {
            craftingButton.visible = true;
            furnaceButton.visible = true;
        }
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.BUILD;
    }
}
