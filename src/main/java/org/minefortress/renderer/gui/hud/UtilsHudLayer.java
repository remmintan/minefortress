package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.remmintan.mods.minefortress.core.dtos.SupportLevel;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.IFortressPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.tasks.IClientTasksHolder;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.gui.DonationScreen;
import net.remmintan.mods.minefortress.gui.hud.HudState;
import net.remmintan.mods.minefortress.gui.widget.hud.HideableButtonWidget;
import net.remmintan.mods.minefortress.gui.widget.hud.ItemButtonWidget;
import net.remmintan.mods.minefortress.gui.widget.hud.ItemToggleOtherItemWidget;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundSleepPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;

import java.util.EnumSet;
import java.util.function.Supplier;

public class UtilsHudLayer extends AbstractHudLayer {


    protected UtilsHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(5, 5, PositionX.LEFT, PositionY.TOP);

        // Supplier to determine if the donation button should be rendered
        Supplier<Boolean> shouldRenderDonationButton = () -> {
            if (this.client.player instanceof IFortressPlayerEntity fortressPlayer) {
                return fortressPlayer.get_SupportLevel() == SupportLevel.NO_SUPPORT;
            }
            return true; // Default to rendering if player is not available or not the right type
        };

        this.addElement(
                new ItemButtonWidget(
                        0,
                        50,
                        Items.CAMPFIRE,
                        btn -> ClientModUtils.getFortressManager().jumpToCampfire(),
                        "Jump to Campfire"
                ),
                new ItemButtonWidget(
                        0,
                        75,
                        Items.RED_BED,
                        btn -> {
                            final var player = MinecraftClient.getInstance().player;
                            if (player != null && !player.isSleeping()) {
                                FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_SLEEP, new ServerboundSleepPacket());
                            }
                        },
                        "Skip Night"
                ),
                new ItemToggleOtherItemWidget(
                        0,
                        100,
                        Items.ENDER_EYE,
                        (btn) -> ClientModUtils.getClientTasksHolder().ifPresent(IClientTasksHolder::toggleSelectionVisibility),
                        (button) -> ClientModUtils.getClientTasksHolder().map(IClientTasksHolder::isSelectionHidden)
                                .map(it -> it ? "Show Tasks outline" : "Hide Tasks outline"),
                        () -> ClientModUtils.getClientTasksHolder().map(IClientTasksHolder::isSelectionHidden).orElse(false),
                        () -> true,
                        Items.ENDER_PEARL
                ),
//                new TextButtonWidget( // Existing Help button - assuming this is still needed as TextButtonWidget
//                        0,
//                        125, // Current Y position
//                        20,
//                        20,
//                        "?",
//                        btn -> {
//                            final BookScreen questionsScreen = new BookScreen(new FortressBookContents(FortressBookContents.HELP_BOOK));
//                            this.client.setScreen(questionsScreen);
//                        },
//                        "key.minefortress.help_button.tooltip" // Using translatable key
//                ),
                // START OF MODIFIED BUTTON
                new HideableButtonWidget( // Changed to HideableButtonWidget
                        0,
                        125, // Position below the help button
                        20,
                        20,
                        "$", // Text for the button
                        btn -> {
                            this.client.setScreen(new DonationScreen()); // Open the new screen
                        },
                        "key.minefortress.donation_button.tooltip", // Translatable tooltip
                        shouldRenderDonationButton // Supplier for visibility
                )
                // END OF MODIFIED BUTTON
        );
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return EnumSet.of(HudState.AREAS_SELECTION, HudState.BUILD, HudState.COMBAT).contains(hudState);
    }
}