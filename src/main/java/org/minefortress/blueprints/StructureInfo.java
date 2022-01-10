package org.minefortress.blueprints;

import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class StructureInfo {

    private final String name;
    private final String file;

    private BlockRotation rotation = BlockRotation.NONE;

    public StructureInfo(String name, String file) {
        this.name = name;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public String getId() {
        return file +"-"+rotation.name();
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
