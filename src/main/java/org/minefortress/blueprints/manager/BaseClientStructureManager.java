package org.minefortress.blueprints.manager;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.remmintan.mods.minefortress.blocks.FortressBlocks;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.*;
import net.remmintan.mods.minefortress.core.utils.BuildingHelper;
import net.remmintan.mods.minefortress.core.utils.ClientExtensionsKt;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseClientStructureManager implements IStructureRenderInfoProvider {

    private final MinecraftClient client;
    private boolean enoughResources = true;
    private boolean cantBuild = false;
    private boolean intersectsUpgradingBuilding = false;

    private BlockPos structureBuildPos = null;

    protected BaseClientStructureManager(MinecraftClient client) {
        this.client = client;
    }

    protected abstract IBlockDataProvider getBlockDataProvider();

    public abstract BlueprintMetadata getSelectedStructure();

    public abstract IBlueprintRotation getSelectedRotation();
    public void tick() {
        if(!isSelecting()) return;
        structureBuildPos = getSelectedPos();
        if (structureBuildPos == null) {
            cantBuild = true;
            enoughResources = true; // Reset to avoid false positives if placement becomes valid later
            intersectsUpgradingBuilding = true; // Reset for the same reason
            return;
        }
        // Reset cantBuild before checks if structureBuildPos is valid
        cantBuild = false;
        checkNotEnoughResources(); // This might set cantBuild = true
        if (cantBuild) return; // Early exit if not enough resources

        // For upgrading logic
        if (this instanceof ClientBlueprintManager clientBlueprintManager && clientBlueprintManager.isUpgrading()) {
            checkIntersectUpgradingBuilding(); // This sets the 'intersectsUpgradingBuilding' field
            if (!intersectsUpgradingBuilding) { // If checkIntersectUpgradingBuilding resulted in false for an upgrade
                cantBuild = true;
            }
            if (cantBuild) return; // Early exit if upgrade intersection fails or other reasons
        } else {
            // If not upgrading, intersectsUpgradingBuilding is generally true (no failure due to intersection).
            // This prevents "doesn't intersect" warnings/logic when not in upgrade mode.
            intersectsUpgradingBuilding = true;
        }

        checkPlacementValidity(); // Combined check for leaves, surface collision, sub-floor validity, and air under floor
    }
    protected BlockPos getStructureBuildPos() {
        return structureBuildPos;
    }
    private void checkNotEnoughResources() {
        final var selectedStructure = getSelectedStructure();
        if (selectedStructure == null) { // Should not happen if isSelecting() is true, but good for safety
            enoughResources = false;
            cantBuild = true;
            return;
        }
        final var campfire = selectedStructure.getId().equals("campfire");
        if (ClientExtensionsKt.isSurvivalFortress(MinecraftClient.getInstance()) && !campfire) {
            final var fortressClientManager = ClientModUtils.getFortressManager();
            final var resourceManager = fortressClientManager.getResourceManager();
            final var stacks = getBlockData().getStacks();
            enoughResources = resourceManager.hasItems(stacks);
        } else {
            enoughResources = true;
        }
        if(!enoughResources) {
            cantBuild = true;
        }
    }

    private void checkPlacementValidity() {
        if (cantBuild) return; // If already marked as can't build (e.g., due to resources), skip further checks
        if (this.client.world == null || structureBuildPos == null) { // Essential objects check
            cantBuild = true;
            return;
        }

        final IStructureBlockData blockData = getBlockData();
        final BlueprintMetadata selectedStructure = getSelectedStructure();
        if (selectedStructure == null) { // Should be caught by isSelecting or resource check, but for safety
            cantBuild = true;
            return;
        }

        // Get all non-air block positions from the blueprint in its local coordinate system
        final Set<BlockPos> blueprintNonAirLocalPositions = blockData.getLayer(BlueprintDataLayer.GENERAL)
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isAir())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        final int floorLevel = selectedStructure.getFloorLevel();
        final Vec3i blueprintSize = blockData.getSize();

        // 1. Check for leaves directly under the blueprint's entire footprint
        // The footprint ground level is 1 Y-level below the blueprint's origin (Y=0).
        // World Y for blueprint's Y=0 is structureBuildPos.getY() - floorLevel.
        // So, ground check Y is structureBuildPos.getY() - floorLevel - 1.
        final int groundYInWorldForLeavesCheck = structureBuildPos.getY() - floorLevel - 1;
        // Iterate over the footprint of the blueprint in world coordinates
        for (int dx = 0; dx < blueprintSize.getX(); dx++) {
            for (int dz = 0; dz < blueprintSize.getZ(); dz++) {
                // structureBuildPos.getX/Z() is minX/Z of structure in world.
                BlockPos currentPosUnderFootprint = new BlockPos(
                        structureBuildPos.getX() + dx,
                        groundYInWorldForLeavesCheck,
                        structureBuildPos.getZ() + dz
                );
                // client.world.getBlockState handles out-of-bounds Y by returning air/void_air, which are not leaves.
                BlockState stateBeneath = this.client.world.getBlockState(currentPosUnderFootprint);
                if (stateBeneath.isIn(BlockTags.LEAVES)) {
                    cantBuild = true;
                    return;
                }
            }
        }

        // 2. NEW: Check for air directly under the blueprint's "floor" blocks.
        // "Floor" blocks are non-air blueprint blocks at localY == floorLevel.
        // Their world position is structureBuildPos.add(localX, 0, localZ)
        // because structureBuildPos.getY() is already the world Y-level of the floor.
        final boolean airUnderneathFloorBlocks = blueprintNonAirLocalPositions.stream()
                .filter(bpLocalPos -> bpLocalPos.getY() == floorLevel) // Filter for blueprint's floor surface blocks
                .map(bpFloorLocalPos -> {
                    // Calculate world position of this blueprint floor block
                    BlockPos worldFloorBlockPos = structureBuildPos.add(
                            bpFloorLocalPos.getX(),
                            0, // Y-offset is 0 because structureBuildPos.getY() is the world Y for floorLevel
                            bpFloorLocalPos.getZ()
                    );
                    return worldFloorBlockPos.down(); // Get the position directly under it in the world
                })
                .anyMatch(posUnderFloor -> {
                    // client.world.getBlockState handles Y bounds (returns AIR or VOID_AIR, both .isAir())
                    return BuildingHelper.canPlaceBlock(this.client.world, posUnderFloor);
                });

        if (airUnderneathFloorBlocks) {
            cantBuild = true;
            return;
        }

        // 3. Check blocks AT OR ABOVE the blueprint's floor level for collisions
        final boolean isUpgrading = (this instanceof ClientBlueprintManager cbm) && cbm.isUpgrading();
        final BlockBox upgradingBox = isUpgrading ? ((ClientBlueprintManager) this).getUpgradingBuildingBox() : null;

        final boolean partAtOrAboveFloorCollides = blueprintNonAirLocalPositions.stream()
                .filter(bpLocalPos -> bpLocalPos.getY() >= floorLevel) // Consider blueprint blocks at or above its floor
                .map(bpLocalPos -> structureBuildPos.add(bpLocalPos.getX(), bpLocalPos.getY() - floorLevel, bpLocalPos.getZ())) // Map to world pos
                .anyMatch(worldPos -> {
                    boolean currentCanPlace = BuildingHelper.canPlaceBlock(client.world, worldPos);
                    if (isUpgrading && upgradingBox != null && upgradingBox.contains(worldPos)) {
                        // If upgrading, and this block is part of the existing structure, it's "placeable" in context.
                        currentCanPlace = true;
                    }
                    return !currentCanPlace; // Collision if cannot place (and not an allowed overlap during upgrade)
                });

        if (partAtOrAboveFloorCollides) {
            cantBuild = true;
            return;
        }

        // 4. Check blocks BELOW the blueprint's floor level (foundation/sub-structure parts)
        // Original logic: These parts must NOT be placeable into air/replaceable blocks,
        // i.e., they must be placed into solid, non-replaceable ground.
        final boolean partBelowFloorInvalid = blueprintNonAirLocalPositions.stream()
                .filter(bpLocalPos -> bpLocalPos.getY() < floorLevel) // Consider blueprint blocks below its floor
                .map(bpLocalPos -> structureBuildPos.add(bpLocalPos.getX(), bpLocalPos.getY() - floorLevel, bpLocalPos.getZ())) // Map to world pos
                .anyMatch(worldPos -> BuildingHelper.canPlaceBlock(client.world, worldPos)); // Invalid if it *can* be placed (i.e., targets air/replaceable)

        if (partBelowFloorInvalid) {
            cantBuild = true;
            // Original code does not return here, allowing cantBuild to be set and fall through.
            // This is fine as canBuild() will reflect the final state of cantBuild.
        }
    }


    private void checkIntersectUpgradingBuilding() {
        if (!(this instanceof ClientBlueprintManager cbm) || !cbm.isUpgrading()) {
            intersectsUpgradingBuilding = true;
            return;
        }

        if (!enoughResources) {
            intersectsUpgradingBuilding = false; // As per original logic, if not enough resources, it's marked as not intersecting for UI/logic consistency.
            return;
        }

        final IStructureBlockData blockData = getBlockData();
        final BlueprintMetadata selectedStructure = getSelectedStructure();
        if (selectedStructure == null) { // Safety check
            intersectsUpgradingBuilding = false;
            return;
        }
        final Set<BlockPos> blueprintDataPositions = blockData.getLayer(BlueprintDataLayer.GENERAL)
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isAir())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        final int floorLevel = selectedStructure.getFloorLevel();
        final BlockBox upgradingBuildingBox = cbm.getUpgradingBuildingBox();
        if (upgradingBuildingBox == null) { // Should ideally not happen if cbm.isUpgrading() is true and a valid building is targeted
            intersectsUpgradingBuilding = false;
            return;
        }

        intersectsUpgradingBuilding = blueprintDataPositions.stream()
                .filter(bpLocalPos -> bpLocalPos.getY() >= floorLevel)
                .map(bpLocalPos -> structureBuildPos.add(bpLocalPos.getX(), bpLocalPos.getY() - floorLevel, bpLocalPos.getZ()))
                .anyMatch(upgradingBuildingBox::contains);
    }

    private IStructureBlockData getBlockData() {
        final var blockDataProvider = getBlockDataProvider();
        final var selected = getSelectedStructure();
        if (selected == null) throw new IllegalStateException("No structure selected");
        final var rotation = getSelectedRotation();
        if (rotation == null) throw new IllegalStateException("No rotation selected");
        return blockDataProvider.getBlockData(selected.getId(), rotation.getRotation());
    }

    @Nullable
    private BlockPos getSelectedPos() {
        if (client.crosshairTarget instanceof final BlockHitResult crosshairTarget && this instanceof ClientBlueprintManager cbm) {
            final BlockPos originalPos = crosshairTarget.getBlockPos();
            if (client.world == null || originalPos == null) return null;

            final BlockState blockState = client.world.getBlockState(originalPos);
            if (blockState.isAir() && !cbm.isUpgrading()) return null;

            var movedPos = originalPos;
            final BlockBox upgradingBuildingBox = cbm.isUpgrading() ? cbm.getUpgradingBuildingBox() : null;
            final boolean intersectingWithTheBuilding = cbm.isUpgrading() && upgradingBuildingBox != null &&
                    (upgradingBuildingBox.contains(originalPos) || blockState.isOf(FortressBlocks.FORTRESS_BUILDING));

            if (intersectingWithTheBuilding) {
                movedPos = new BlockPos(movedPos.getX(), upgradingBuildingBox.getMinY(), movedPos.getZ());
            }

            return moveToStructureSize(movedPos, intersectingWithTheBuilding);

        }
        return null;
    }

    private BlockPos moveToStructureSize(BlockPos pos, boolean intersectingWithTheBuilding) {
        if(getSelectedStructure() == null) return pos;

        final boolean posSolid = BuildingHelper.hasCollisions(client.world, pos) && !intersectingWithTheBuilding;
        final IStructureBlockData blockData = getBlockData();
        final Vec3i size = blockData.getSize();
        final Vec3i halfSize = new Vec3i(size.getX() / 2, 0, size.getZ() / 2);
        BlockPos movedPos = pos.subtract(halfSize);
        // If placing on a solid block (and not upgrading an existing structure at that spot),
        // move the placement position up by one.
        if (posSolid) {
            movedPos = movedPos.up();
        }
        return movedPos;
    }

    @Override
    public final Optional<BlockPos> getStructureRenderPos() {
        final var selected = getSelectedStructure();
        if (selected == null) return Optional.empty();
        final var floorLevel = selected.getFloorLevel();
        // structureBuildPos.getY() is world Y for blueprint's floorLevel.
        // structureBuildPos.down(floorLevel) results in world Y for blueprint's Y=0.
        return Optional.ofNullable(structureBuildPos).map(it -> it.down(floorLevel));
    }

    @Override
    public boolean canBuild() {
        return !cantBuild;
    }

    @Override
    public boolean intersectsUpgradingBuilding() {
        return intersectsUpgradingBuilding;
    }

    protected void reset() {
        structureBuildPos = null;
        cantBuild = false;
        enoughResources = true;
        intersectsUpgradingBuilding = true; // Default to true, will be set to false if upgrading and not intersecting
        getBlockDataProvider().reset();
    }

}