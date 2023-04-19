package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.Items;
import org.minefortress.network.c2s.ServerboundSleepPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.FortressBookContents;
import org.minefortress.renderer.gui.widget.ItemButtonWidget;
import org.minefortress.renderer.gui.widget.ItemToggleOtherItemWidget;
import org.minefortress.renderer.gui.widget.TextButtonWidget;
import org.minefortress.tasks.ClientVisualTasksHolder;
import org.minefortress.utils.ModUtils;

import java.util.EnumSet;

public class UtilsHudLayer extends AbstractHudLayer {


    protected UtilsHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(25, 5, PositionX.LEFT, PositionY.TOP);

        this.addElement(
            new ItemButtonWidget(
                    0,
                    100,
                    Items.CAMPFIRE,
                    btn -> ModUtils.getFortressClientManager().jumpToCampfire(),
                    "Jump to Campfire"
            ),
            new ItemButtonWidget(
                    0,
                    125,
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
                    150,
                    Items.ENDER_EYE,
                    (btn) -> ModUtils.getClientTasksHolder().ifPresent(ClientVisualTasksHolder::toggleSelectionVisibility),
                    (button) -> ModUtils.getClientTasksHolder().map(ClientVisualTasksHolder::isSelectionHidden)
                            .map(it -> it ? "Show Tasks outline" : "Hide Tasks outline"),
                    () -> ModUtils.getClientTasksHolder().map(ClientVisualTasksHolder::isSelectionHidden).orElse(false),
                    () -> true,
                    Items.ENDER_PEARL
            ),
            new TextButtonWidget(
                    0,
                    175,
                    20,
                    20,
                    "?",
                    btn -> {
                        final BookScreen questionsScreen = new BookScreen(new FortressBookContents(FortressBookContents.HELP_BOOK));
                        this.client.setScreen(questionsScreen);
                    },
                    "Help"
            )
        );
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return EnumSet.of(HudState.AREAS_SELECTION, HudState.BUILD, HudState.COMBAT).contains(hudState);
    }
}
