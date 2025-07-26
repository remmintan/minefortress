package net.remmintan.mods.minefortress.core.interfaces.buildings;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata;
import net.remmintan.mods.minefortress.core.dtos.buildings.HudBar;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IFortressBuilding {

    default BlockPos getPos() {
        if (this instanceof BlockEntity be) {
            return be.getPos();
        } else {
            throw new IllegalStateException("This building has no associated block entity!");
        }
    }

    @Nullable
    BlockPos getRandomPosToComeToBuilding();

    default List<String> getUpgrades() {
        return getMetadata().getRequirement().getUpgrades();
    }

    Optional<IAutomationArea> getAutomationArea();

    int getHealth();

    BlockPos getStart();

    BlockPos getEnd();

    void destroy();

    BlueprintMetadata getMetadata();

    default BlockPos getCenter() {
        final var start = getStart();
        final var end = getEnd();
        return BlockBox.create(start, end).getCenter();
    }

    default BlockPos getNearestCornerXZ(BlockPos pos, World world) {
        final var start = getStart();
        final var end = getEnd();
        final var x = pos.getX() < start.getX() ? start.getX() : Math.min(pos.getX(), end.getX());
        final var z = pos.getZ() < start.getZ() ? start.getZ() : Math.min(pos.getZ(), end.getZ());
        return world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z));
    }

    default boolean isPartOfTheBuilding(BlockPos pos) {
        final var start = getStart().toImmutable().down(1);
        final var end = getEnd().toImmutable().up(1);

        return BlockBox.create(start, end).contains(pos);
    }

    Optional<BlockPos> getFreeBed(World world);

    boolean satisfiesRequirement(ProfessionType type, int level);

    void attack(HostileEntity attacker);

    Set<HostileEntity> getAttackers();

    List<ItemStack> getRepairItemInfos();

    Map<BlockPos, BlockState> getBlocksToRepair();

    List<BlockPos> getFurnacePos();

    void findFurnaces();

    IBuildingHireHandler getHireHandler();

    List<HudBar> getBars();
}
