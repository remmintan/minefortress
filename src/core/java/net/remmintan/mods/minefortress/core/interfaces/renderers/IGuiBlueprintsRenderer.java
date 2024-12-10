package net.remmintan.mods.minefortress.core.interfaces.renderers;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockRotation;

public interface IGuiBlueprintsRenderer {
    void renderBlueprintPreview(MatrixStack matrices, String fileName, BlockRotation blockRotation);

    void renderBlueprintInGui(MatrixStack matrices, String blueprintId, BlockRotation blockRotation, float anchorX, float anchorY, int slotColumn, int slotRow, boolean isEnoughResources);

    void renderBlueprintInGui(MatrixStack matrices, String blueprintId, BlockRotation blockRotation, int slotColumn, int slotRow, boolean isEnoughResources);
}
