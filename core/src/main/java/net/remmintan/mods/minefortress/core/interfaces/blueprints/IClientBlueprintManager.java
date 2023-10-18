package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.nbt.NbtCompound;

import java.util.List;

public interface IClientBlueprintManager extends IStructureRenderInfoProvider {

    void handleBlueprintsImport();
    void handleBlueprintsExport(String name, byte[] bytes);
    void handleImportExportFailure();

    void select(IBlueprintMetadata blueprintMetadata);

    void selectNext();

    List<IBlueprintMetadata> getAllBlueprints(BlueprintGroup group);

    void buildCurrentStructure();

    void clearStructure();

    IBlueprintMetadataManager getBlueprintMetadataManager();

    void rotateSelectedStructureClockwise();

    void rotateSelectedStructureCounterClockwise();

    IBlockDataProvider getBlockDataProvider();

    void add(BlueprintGroup group, String name, String file, int floorLevel, String requirementId, NbtCompound tag);

    void update(String fileName, NbtCompound tag, int newFloorLevel);

    void remove(String filename);

    void reset();
}
