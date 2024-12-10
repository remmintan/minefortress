package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.nbt.NbtCompound;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;

import java.util.List;

public interface IClientBlueprintManager extends IStructureRenderInfoProvider {
    void tick();

    void select(BlueprintMetadata blueprintMetadata);

    default void select(String blueprintId) {
        getBlueprintMetadataManager().getByBlueprintId(blueprintId).ifPresent(this::select);
    }

    List<BlueprintMetadata> getAllBlueprints(BlueprintGroup group);

    void buildCurrentStructure();

    void clearStructure();

    IBlueprintMetadataManager getBlueprintMetadataManager();

    void rotateSelectedStructureClockwise();

    void rotateSelectedStructureCounterClockwise();

    IBlockDataProvider getBlockDataProvider();

    void sync(BlueprintMetadata metadata, NbtCompound tag);

    void remove(String blueprintId);

    void reset();

    void updateSlotsInBlueprintsScreen();
}
