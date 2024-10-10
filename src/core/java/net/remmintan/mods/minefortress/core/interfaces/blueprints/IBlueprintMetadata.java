package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.util.BlockRotation;

public interface IBlueprintMetadata {
    String getName();

    String getId();

    IBlueprintRequirement getRequirement();

    int getFloorLevel();

    void setFloorLevel(int floorLevel);

    void rotateRight();

    void rotateLeft();

    BlockRotation getRotation();
}
