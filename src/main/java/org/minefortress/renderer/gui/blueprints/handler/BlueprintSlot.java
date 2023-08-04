package org.minefortress.renderer.gui.blueprints.handler;

import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import org.minefortress.blueprints.data.StrctureBlockData;
import org.minefortress.blueprints.manager.BlueprintMetadata;

public class BlueprintSlot {

    public static final BlueprintSlot EMPTY = new BlueprintSlot();

    private final BlueprintMetadata metadata;
    private final boolean enoughResources;
    private final Text tooltipText;
    private final StrctureBlockData blockData;

    private BlueprintSlot() {
        metadata = null;
        enoughResources = true;
        tooltipText = Text.literal("");
        blockData = null;
    }

    public BlueprintSlot(BlueprintMetadata metadata, boolean enoughResources, StrctureBlockData blockData) {
        this.metadata = metadata;
        this.tooltipText = new LiteralTextContent(metadata.getName());
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

    public StrctureBlockData getBlockData() {
        return blockData;
    }
}
