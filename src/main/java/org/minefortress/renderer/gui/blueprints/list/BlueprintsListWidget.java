package org.minefortress.renderer.gui.blueprints.list;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;

public class BlueprintsListWidget extends AlwaysSelectedEntryListWidget<BlueprintListEntry> {

    public BlueprintsListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.right - 6;
    }

}
