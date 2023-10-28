package org.minefortress.renderer.gui.blueprints.handler;

import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadata;

public class BlueprintSlot {

    public static final BlueprintSlot EMPTY = new BlueprintSlot();

    private final IBlueprintMetadata metadata;
    private final boolean enoughResources;
    private final Text tooltipText;
    private final IStructureBlockData blockData;

    private BlueprintSlot() {
        metadata = null;
        enoughResources = true;
        tooltipText = Text.literal("");
        blockData = null;
    }

    public BlueprintSlot(IBlueprintMetadata metadata, boolean enoughResources, IStructureBlockData blockData) {
        this.metadata = metadata;
        this.tooltipText = Text.literal(metadata.getName());
        this.enoughResources = enoughResources;
        this.blockData = blockData;
    }

    public Text getTooltipText() {
        return tooltipText;
    }

    public IBlueprintMetadata getMetadata() {
        return metadata;
    }

    public boolean isEnoughResources() {
        return enoughResources;
    }

    public IStructureBlockData getBlockData() {
        return blockData;
    }
}
