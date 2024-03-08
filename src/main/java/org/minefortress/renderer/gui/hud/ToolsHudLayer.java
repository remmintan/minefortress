package org.minefortress.renderer.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.remmintan.gobi.SelectionType;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.utils.CoreModUtils;
import org.minefortress.renderer.gui.blueprints.BlueprintsScreen;
import org.minefortress.renderer.gui.widget.HideableButtonWidget;
import org.minefortress.renderer.gui.widget.ItemButtonWidget;
import org.minefortress.renderer.gui.widget.ItemToggleWidget;
import org.minefortress.renderer.gui.widget.ModeButtonWidget;
import org.minefortress.utils.ModUtils;

import java.util.Arrays;
import java.util.Optional;

public class ToolsHudLayer extends AbstractHudLayer {

    private final ItemButtonWidget selection;
    private final ModeButtonWidget buildEditMode;

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
                        () -> !treeCutterSelected() && !roadsSelected() && hudHasCorrectState(FortressState.BUILD_EDITING)
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
                        () -> !blueprintSelected() && !roadsSelected() && hudHasCorrectState(FortressState.BUILD_EDITING)
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
                        () -> !blueprintSelected() && !treeCutterSelected() && hudHasCorrectState(FortressState.BUILD_EDITING)
                )
        );

        buildEditMode = new ModeButtonWidget(
                0,
                125,
                Items.BRICKS,
                btn -> setCorrectHudState(FortressState.BUILD_EDITING),
                () -> isAnyPawnSelected()? "Building Mode" : "Select any pawn to build",
                () -> hudHasCorrectState(FortressState.BUILD_EDITING)
        );
        this.addElement(
                buildEditMode,
                new ModeButtonWidget(
                        0,
                        150,
                        Items.COMPASS,
                        btn -> setCorrectHudState(FortressState.BUILD_SELECTION),
                        "Selection Mode",
                        () -> hudHasCorrectState(FortressState.BUILD_SELECTION)
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
                            type.name(),
                            () -> selection.checked && hudHasCorrectState(FortressState.BUILD_EDITING)
                    )
            );
        }
    }

    private boolean hudHasCorrectState(FortressState expectedState) {
        return ModUtils.getFortressClientManager().getState() == expectedState;
    }

    private void setCorrectHudState(FortressState expectedState) {
        ModUtils.getFortressClientManager().setState(expectedState);
    }

    @Override
    public void tick() {
        super.tick();
        selection.visible = hudHasCorrectState(FortressState.BUILD_EDITING);
        buildEditMode.active = isAnyPawnSelected();
    }

    private static boolean isAnyPawnSelected() {
        return CoreModUtils.getMineFortressManagersProvider().get_PawnsSelectionManager().hasSelected();
    }

    @Override
    public boolean shouldRender(HudState hudState) {
        return hudState == HudState.BUILD;
    }

    private boolean treeCutterSelected() {
        return CoreModUtils.getMineFortressManagersProvider().get_SelectionManager().getSelectionTypeIndex() == SelectionType.TREE.ordinal();
    }

    private boolean roadsSelected() {
        return CoreModUtils.getMineFortressManagersProvider().get_SelectionManager().getSelectionTypeIndex() == SelectionType.ROADS.ordinal();
    }

    private boolean blueprintSelected() {
        return CoreModUtils.getMineFortressManagersProvider().get_BlueprintManager().isSelecting();
    }

}
