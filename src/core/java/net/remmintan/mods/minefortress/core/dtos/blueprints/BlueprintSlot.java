package net.remmintan.mods.minefortress.core.dtos.blueprints;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData;

import java.util.ArrayList;
import java.util.List;

public class BlueprintSlot {

    public static final BlueprintSlot EMPTY = new BlueprintSlot();

    private final BlueprintMetadata metadata;
    private final boolean enoughResources;
    private final List<Text> tooltipText;
    private final IStructureBlockData blockData;

    private BlueprintSlot() {
        metadata = null;
        enoughResources = true;
        tooltipText = List.of(Text.literal(""));
        blockData = null;
    }

    public BlueprintSlot(BlueprintMetadata metadata, boolean enoughResources, IStructureBlockData blockData) {
        this.metadata = metadata;

        this.tooltipText = new ArrayList<>();

        final var name = Text.of(metadata.getName());
        tooltipText.add(name);
        final var requirement = metadata.getRequirement();
        final var type = requirement.getType();
        if (type != null) {
            final var displayName = type.getDisplayName();
            final var unlocksText = Text.literal("Unlocks: ").append(displayName).formatted(Formatting.GRAY);
            tooltipText.add(unlocksText);

            final var level = requirement.getLevel();
            final var totalLevels = requirement.getTotalLevels();
            final var levelText = Text.literal("Level: ")
                    .append(String.valueOf(level + 1))
                    .append("/")
                    .append(String.valueOf(totalLevels))
                    .formatted(Formatting.GRAY);
            tooltipText.add(levelText);
        }

        final var villagersCapacity = this.metadata.getCapacity();
        if (villagersCapacity > 0) {
            final var villagersText = Text.literal("Capacity: ")
                    .append(String.valueOf(villagersCapacity))
                    .formatted(Formatting.GRAY);
            tooltipText.add(villagersText);
        }


        this.enoughResources = enoughResources;
        this.blockData = blockData;
    }

    public List<Text> getTooltipText() {
        return tooltipText;
    }

    public BlueprintMetadata getMetadata() {
        return metadata;
    }

    public List<Text> getUpgradeTooltipText(int currentBuildingLevel, int currentCapacity) {
        final var tooltip = new ArrayList<Text>();
        final var name = Text.of(metadata.getName());
        tooltip.add(name);

        final var requirement = metadata.getRequirement();
        final var type = requirement.getType();
        if (type != null) {
            final var level = requirement.getLevel();
            if (level > currentBuildingLevel + 1) {
                final var unlockPreviousText = Text.literal("First, unlock previous level").formatted(Formatting.RED);
                tooltip.add(unlockPreviousText);
                return tooltip;
            }

            final var totalLevels = requirement.getTotalLevels();
            final var levelText = Text.literal("Level: ")
                    .append(String.valueOf(level + 1))
                    .append("/")
                    .append(String.valueOf(totalLevels))
                    .formatted(Formatting.GRAY);
            tooltip.add(levelText);
        }

        final var villagersCapacity = this.metadata.getCapacity();
        if (villagersCapacity > 0) {
            final var villagersText = Text.literal("Capacity: ")
                    .append(String.valueOf(villagersCapacity - currentCapacity))
                    .formatted(Formatting.GRAY);
            tooltip.add(villagersText);
        }

        final var hireSpeedText = Text.literal("Hire speed: ")
                .append("x2")
                .formatted(Formatting.GRAY);
        tooltip.add(hireSpeedText);

        if (!enoughResources) {
            final var notEnoughResourcesText = Text.literal("Not enough resources").formatted(Formatting.RED);
            tooltip.add(notEnoughResourcesText);
        }

        return tooltip;
    }

    public boolean isEnoughResources() {
        return enoughResources;
    }

    public IStructureBlockData getBlockData() {
        return blockData;
    }
}
