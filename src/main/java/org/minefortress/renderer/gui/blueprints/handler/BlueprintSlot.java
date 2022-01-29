package org.minefortress.renderer.gui.blueprints.handler;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.minefortress.blueprints.BlueprintMetadata;

public class BlueprintSlot {

    private final BlueprintMetadata metadata;
    private final Text tooltipText;

    public BlueprintSlot(BlueprintMetadata metadata) {
        this.metadata = metadata;
        this.tooltipText = new LiteralText(metadata.getName());
    }

    public Text getTooltipText() {
        return tooltipText;
    }

    public BlueprintMetadata getMetadata() {
        return metadata;
    }

}
