package org.minefortress.renderer.gui.fortress;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.minefortress.blueprints.manager.BlueprintMetadata;
import org.minefortress.fortress.buildings.EssentialBuildingInfo;
import org.minefortress.network.c2s.C2SDestroyBuilding;
import org.minefortress.network.c2s.C2SOpenRepairBuildingScreen;
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
        final var xBtn = ButtonWidget
                .builder(
                        Text.literal("X"),
                        button -> super.closeScreen()
                )
                .dimensions(getScreenRightX() - 25, getScreenTopY() + 5, 20, 20)
                .build();
        this.addDrawableChild(xBtn);

        final var destroyBuildingBtn = ButtonWidget
                .builder(
                        Text.literal("Destroy Building"),
                        button -> showDestroyConfirmation()
                )
                .dimensions(getScreenCenterX() - 100, getScreenTopY() + 55, 200, 20)
                .build();
        this.addDrawableChild(destroyBuildingBtn);

        if(buildingInfo.getHealth() < 100) {
            this.addDrawableChild(
                    ButtonWidget
                            .builder(
                                    Text.literal("Repair Building"),
                                    button -> openRepairBuildingScreen()
                            )
                            .dimensions(getScreenCenterX() - 100, getScreenTopY() + 80, 200, 20)
                            .build()
            );
        }


        Optional.ofNullable(profession).ifPresent(it -> {
            if(it.isHireMenu()) {
                this.addDrawableChild(
                        ButtonWidget
                                .builder(
                                        Text.literal("Hire pawns"),
                                        button -> ModUtils
                                                .getProfessionManager()
                                                .increaseAmount(it.getId(), false)
                                )
                                .dimensions(getScreenCenterX() - 100, getScreenTopY() + 105, 200, 20)
                                .build()
                );
            }
        });
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.render(drawContext, mouseX, mouseY, delta);
        // render health
        final var healthInPercents = buildingInfo.getHealth();

        final var healthText = Text.of("Health: " + healthInPercents + "%");
        drawContext.drawText(this.textRenderer, healthText, getScreenLeftX() + 30, getScreenTopY() + 30, 0xFFFFFF, false);
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

    private void openRepairBuildingScreen() {
        final var packet = new C2SOpenRepairBuildingScreen(buildingInfo.getId());
        FortressClientNetworkHelper.send(C2SOpenRepairBuildingScreen.CHANNEL, packet);
    }
}
