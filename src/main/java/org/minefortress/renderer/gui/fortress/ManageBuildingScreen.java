package org.minefortress.renderer.gui.fortress;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.minefortress.fortress.automation.EssentialBuildingInfo;

public class ManageBuildingScreen extends Screen {

    private final EssentialBuildingInfo buildingInfo;

    protected ManageBuildingScreen(EssentialBuildingInfo essentialBuildingInfo) {
        super(Text.of("Manage Building"));
        this.buildingInfo = essentialBuildingInfo;
    }
}
