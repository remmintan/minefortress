package org.minefortress.blueprints.manager;

import net.minecraft.util.BlockRotation;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement;

import java.util.Optional;

public class BlueprintMetadata implements IBlueprintMetadata {

    private final String id;
    private final String name;
    private final BlueprintRequirement requirement;
    private int floorLevel;

    private BlockRotation rotation = BlockRotation.NONE;

    public BlueprintMetadata(String name, String id, int floorLevel, String requirementId) {
        this.name = name;
        this.id = id;
        this.floorLevel = floorLevel;
        this.requirement = new BlueprintRequirement(Optional.ofNullable(requirementId).orElse("none"));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public IBlueprintRequirement getRequirement() {
        return requirement;
    }

    @Override
    public int getFloorLevel() {
        return floorLevel;
    }

    @Override
    public void setFloorLevel(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    @Override
    public void rotateRight() {
        rotation = rotation.rotate(BlockRotation.CLOCKWISE_90);
    }

    @Override
    public void rotateLeft() {
        rotation = rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90);
    }

    @Override
    public BlockRotation getRotation() {
        return rotation;
    }

}
