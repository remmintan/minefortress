package org.minefortress.blueprints.manager;

import net.minecraft.util.BlockRotation;

import java.util.Optional;

public class BlueprintMetadata {

    private final String name;
    private final String file;
    private final String requirementId;
    private int floorLevel;

    private BlockRotation rotation = BlockRotation.NONE;

    public BlueprintMetadata(String name, String file, int floorLevel, String requirementId) {
        this.name = name;
        this.file = file;
        this.floorLevel = floorLevel;
        this.requirementId = requirementId;
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public String getRequirementId() {
        return Optional.ofNullable(requirementId).orElse("none");
    }

    public String getId() {
        return file +"-"+rotation.name();
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
