package net.remmintan.mods.minefortress.core.interfaces.automation;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public interface IAutomationAreaInfo {

    void writeToBuffer(PacketByteBuf buf);
    boolean contains(BlockPos pos);

    List<BlockPos> getClientArea();
    ProfessionsSelectionType getAreaType();

    UUID getId();

}
