package net.remmintan.mods.minefortress.core.interfaces.buildings;

import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IFortressBuilding extends IAutomationArea {

    default NamedScreenHandlerFactory getScreenHandlerFactory() {
        if (this instanceof NamedScreenHandlerFactory nshf) {
            return nshf;
        } else {
            throw new UnsupportedOperationException("This building does not have a screen handler factory");
        }
    }

    int getHealth();

    BlockPos getStart();

    BlockPos getEnd();

    void destroy();

    String getName();

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

    int getBedsCount();

    boolean satisfiesRequirement(ProfessionType type, int level);

    void attack(HostileEntity attacker);

    Set<HostileEntity> getAttackers();

    Map<BlockPos, BlockState> getAllBlockStatesToRepairTheBuilding();
}
