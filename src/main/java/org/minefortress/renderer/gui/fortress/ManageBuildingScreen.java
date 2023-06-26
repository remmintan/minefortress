package org.minefortress.renderer.gui.fortress;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.minefortress.blueprints.manager.BlueprintMetadata;
import org.minefortress.fortress.buildings.EssentialBuildingInfo;
import org.minefortress.network.c2s.C2SDestroyBuilding;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.professions.Profession;
import org.minefortress.renderer.gui.WindowScreen;
import org.minefortress.utils.ModUtils;

import java.util.Optional;

public class ManageBuildingScreen extends WindowScreen {

    private final EssentialBuildingInfo buildingInfo;
    private final Profession profession;
    private final Text screenName;

    public ManageBuildingScreen(EssentialBuildingInfo essentialBuildingInfo) {
        super(Text.of("Manage Building"));
        this.buildingInfo = essentialBuildingInfo;
        final var blueprintMetadataManager = ModUtils.getBlueprintManager().getBlueprintMetadataManager();


        screenName = Text.of(
                essentialBuildingInfo
                    .getBlueprintId()
                    .flatMap(blueprintMetadataManager::getByBlueprintId)
                    .map(BlueprintMetadata::getName)
                    .orElse("Unknown")
        );
        profession = ModUtils.getProfessionManager()
                .getByBuildingRequirement(buildingInfo.getRequirementId())
                .orElse(null);
    }

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(new ButtonWidget(
                getScreenRightX() - 25,
                getScreenTopY() + 5,
                20,
                20,
                Text.of("X"),
                button -> super.closeScreen()
        ));

        this.addDrawableChild(
            new ButtonWidget(
                getScreenCenterX() - 100,
                getScreenTopY() + 30,
                200,
                20,
                Text.of("Destroy Building"),
                button -> showDestroyConfirmation()
            )
        );

        Optional.ofNullable(profession).ifPresent(it -> {
            if(it.isHireMenu()) {
                this.addDrawableChild(
                        new ButtonWidget(
                                getScreenCenterX() - 100,
                                getScreenTopY() + 55,
                                200,
                                20,
                                Text.of("Hire pawns"),
                                button -> ModUtils
                                        .getProfessionManager()
                                        .increaseAmount(it.getId(), false)
                        )
                );
            }
        });
    }

    private void showDestroyConfirmation() {
        if (this.client == null) {
            return;
        }
        this.client.setScreen(new ConfirmScreen(
                this::destroyBuilding,
                Text.of("Are you sure you want to destroy this building?"),
                Text.of("You will lose all resources invested in it."),
                Text.of("Destroy"),
                Text.of("Cancel")
        ));
    }

    private void destroyBuilding(boolean confirmed) {
        if (confirmed) {
            final var packet = new C2SDestroyBuilding(buildingInfo.getId());
            FortressClientNetworkHelper.send(C2SDestroyBuilding.CHANNEL, packet);
            super.closeScreen();
        } else {
            if(this.client != null)
                this.client.setScreen(null);
        }
    }

    @Override
    public Text getTitle() {
        return screenName;
    }
}
