package net.remmintan.mods.minefortress.core.interfaces.buildings;

import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IFortressBuilding extends IAutomationArea {
    boolean updateTheHealthState(ServerWorld world);

    int getHealth();

    boolean isPartOfTheBuilding(BlockPos pos);

    BlockPos getStart();

    BlockPos getEnd();

    BlockPos getCenter();

    BlockPos getNearestCornerXZ(BlockPos pos, World world);

    Optional<BlockPos> getFreeBed(World world);

    long getBedsCount(World world);

    void writeToNbt(NbtCompound tag);

    boolean satisfiesRequirement(ProfessionType type, int level);

    UUID getId();

    void attack(HostileEntity attacker);

    Set<HostileEntity> getAttackers();

    IEssentialBuildingInfo toEssentialInfo(World world);

    Map<BlockPos, BlockState> getAllBlockStatesToRepairTheBuilding();
}
