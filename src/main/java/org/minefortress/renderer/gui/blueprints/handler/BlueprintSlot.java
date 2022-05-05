package org.minefortress.renderer.gui.blueprints.handler;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.minefortress.blueprints.data.BlueprintBlockData;
import org.minefortress.blueprints.manager.BlueprintMetadata;

public class BlueprintSlot {

    private final BlueprintMetadata metadata;
    private final boolean enoughResources;
    private final Text tooltipText;
    private final BlueprintBlockData blockData;

    public BlueprintSlot(BlueprintMetadata metadata, boolean enoughResources, BlueprintBlockData blockData) {
        this.metadata = metadata;
        this.tooltipText = new LiteralText(metadata.getName());
        this.enoughResources = enoughResources;
        this.blockData = blockData;
    }

    public Text getTooltipText() {
        return tooltipText;
    }

    public BlueprintMetadata getMetadata() {
        return metadata;
    }

    public boolean isEnoughResources() {
        return enoughResources;
    }

    public BlueprintBlockData getBlockData() {
        return blockData;
    }
}
