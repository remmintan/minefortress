package org.minefortress.blueprints.manager;

import net.minecraft.util.BlockRotation;

public class BlueprintMetadata {

    private final String name;
    private final String file;
    private final String requirementId;
    private final boolean premium;
    private int floorLevel;

    private BlockRotation rotation = BlockRotation.NONE;

    public BlueprintMetadata(String name, String file, String requirementId) {
        this.name = name;
        this.file = file;
        this.requirementId = requirementId;
        this.floorLevel = 0;
        this.premium = false;
    }

    public BlueprintMetadata(String name, String file, String requirementId, boolean premium) {
        this.name = name;
        this.file = file;
        this.requirementId = requirementId;
        this.floorLevel = 0;
        this.premium = premium;
    }

    public BlueprintMetadata(String name, String file, String requirementId, int floorLevel) {
        this.name = name;
        this.file = file;
        this.requirementId = requirementId;
        this.floorLevel = floorLevel;
        this.premium = false;
    }

    public BlueprintMetadata(String name, String file, String requirementId, int floorLevel,  boolean premium) {
        this.name = name;
        this.file = file;
        this.requirementId = requirementId;
        this.floorLevel = floorLevel;
        this.premium = premium;
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public String getRequirementId() {
        return requirementId;
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

    public boolean isPremium() {
        return premium;
    }
}
