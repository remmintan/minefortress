package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.Items;
import org.minefortress.network.c2s.ServerboundSleepPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.FortressBookContents;
import org.minefortress.renderer.gui.blueprints.BlueprintsScreen;
import org.minefortress.renderer.gui.widget.*;
import org.minefortress.selections.SelectionType;
import org.minefortress.tasks.ClientTasksHolder;
import org.minefortress.utils.ModUtils;

import java.util.Arrays;
import java.util.Optional;

public class ToolsHudLayer extends AbstractHudLayer {

    private final ItemButtonWidget selection;

    protected ToolsHudLayer(MinecraftClient client) {
        super(client);
        this.setBasepoint(-25, 5, PositionX.RIGHT, PositionY.TOP);
        selection = new ItemButtonWidget(
                0,
                0,
                Items.DIAMOND_PICKAXE,
                btn -> {
                    final ItemButtonWidget fortressBtn = (ItemButtonWidget) btn;
                    fortressBtn.checked = !fortressBtn.checked;
                },
                (button) -> button.checked ? Optional.empty() : Optional.of("Selection Type")
        );
        this.addElement(selection);


        this.addElement(
                new ItemToggleWidget(
                        0,
                        25,
                        Items.OAK_DOOR,
                        btn -> {
                            if (blueprintSelected())
                                ModUtils.getBlueprintManager().clearStructure();
                            else {
                                this.client.setScreen(new BlueprintsScreen());
                            }
                        },
                        (button) -> Optional.of(blueprintSelected() ? "Cancel" : "Blueprints"),
                        this::blueprintSelected,
                        () -> !treeCutterSelected() && !roadsSelected()
                ),
                new ItemToggleWidget(
                        0,
                        50,
                        Items.DIAMOND_AXE,
                        btn -> {
                            if (treeCutterSelected()) {
                                ModUtils.getSelectionManager().setSelectionType(SelectionType.SQUARES);
                            } else {
                                ModUtils.getSelectionManager().setSelectionType(SelectionType.TREE);
                            }
                        },
                        (button) -> Optional.of(treeCutterSelected() ? "Cancel" : "Chop trees"),
                        this::treeCutterSelected,
                        () -> !blueprintSelected() && !roadsSelected()
                ),
                new ItemToggleWidget(
                        0,
                        75,
                        Items.DIAMOND_SHOVEL,
                        btn -> {
                            if (roadsSelected()) {
                                ModUtils.getSelectionManager().setSelectionType(SelectionType.SQUARES);
                            } else {
                                ModUtils.getSelectionManager().setSelectionType(SelectionType.ROADS);
                            }
                        },
                        (button) -> Optional.of(roadsSelected() ? "Cancel" : "Build roads"),
                        this::roadsSelected,
                        () -> !blueprintSelected() && !treeCutterSelected()
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
                )
        );

        this.addElement(
                new ItemToggleOtherItemWidget(
                        0,
                        150,
                        Items.ENDER_EYE,
                        (btn) -> ModUtils.getClientTasksHolder().ifPresent(ClientTasksHolder::toggleSelectionVisibility),
                        (button) -> ModUtils.getClientTasksHolder().map(ClientTasksHolder::isSelectionHidden)
                                .map(it -> it ? "Show Tasks outline" : "Hide Tasks outline"),
                        () -> ModUtils.getClientTasksHolder().map(ClientTasksHolder::isSelectionHidden).orElse(false),
                        () -> !blueprintSelected() && !treeCutterSelected() && !roadsSelected(),
                        Items.ENDER_PEARL
                ),
                new TextButtonWidget(
                        0, 175, 20, 20, "?",
                        btn -> {
                            final BookScreen questionsScreen = new BookScreen(new FortressBookContents(FortressBookContents.HELP_BOOK));
                            this.client.setScreen(questionsScreen);
                        },
                        "Help"
                )
        );

        final SelectionType[] values = Arrays
                .stream(SelectionType.values())
                .filter(type -> type != SelectionType.TREE)
                .filter(type -> type != SelectionType.ROADS)
                .toArray(SelectionType[]::new);

        var i = 0;
        for (final SelectionType type : values) {
            this.addElement(
                    new HideableButtonWidget(
                            -35,
                            25 * i++,
                            20,
                            20,
                            type.getButtonText(),
                            btn -> ModUtils.getSelectionManager().setSelectionType(type),
                            type.getName(),
                            () -> selection.checked
                    )
            );
        }
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.BUILD;
    }

    private boolean treeCutterSelected() {
        return ModUtils.getFortressClient().getSelectionManager().getSelectionTypeIndex() == SelectionType.TREE.ordinal();
    }

    private boolean roadsSelected() {
        return ModUtils.getFortressClient().getSelectionManager().getSelectionTypeIndex() == SelectionType.ROADS.ordinal();
    }

    private boolean blueprintSelected() {
        return ModUtils.getFortressClient().getBlueprintManager().hasSelectedBlueprint();
    }

}
