package org.minefortress.blueprints.manager;

import net.minecraft.util.BlockRotation;

import java.util.Optional;

public class BlueprintMetadata {

    private final String id;
    private final String name;
    private final String requirementId;
    private int floorLevel;

    private BlockRotation rotation = BlockRotation.NONE;

    public BlueprintMetadata(String name, String id, int floorLevel, String requirementId) {
        this.name = name;
        this.id = id;
        this.floorLevel = floorLevel;
        this.requirementId = requirementId;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getRequirementId() {
        return Optional.ofNullable(requirementId).orElse("none");
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public void setFloorLevel(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    public void rotateRight() {
        rotation = rotation.rotate(BlockRotation.CLOCKWISE_90);
    }

    public void rotateLeft() {
        rotation = rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90);
    }

    public BlockRotation getRotation() {
        return rotation;
    }

}
