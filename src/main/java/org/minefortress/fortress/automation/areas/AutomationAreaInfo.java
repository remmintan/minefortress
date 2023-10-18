package org.minefortress.fortress.automation.areas;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaInfo;
import net.remmintan.mods.minefortress.core.interfaces.automation.ProfessionsSelectionType;

import java.util.List;
import java.util.UUID;

public class AutomationAreaInfo implements IAutomationAreaInfo {

    protected List<BlockPos> area;
    private final ProfessionsSelectionType areaType;
    private final UUID id;

    public AutomationAreaInfo(List<BlockPos> area, ProfessionsSelectionType areaType, UUID id) {
        this.area = area;
        this.areaType = areaType;
        this.id = id;
    }

    public void writeToBuffer(PacketByteBuf buf) {
        buf.writeVarInt(area.size());
        for(BlockPos pos: area) {
            buf.writeBlockPos(pos);
        }
        buf.writeEnumConstant(areaType);
        buf.writeUuid(id);
    }

    public boolean contains(BlockPos pos) {
        return area.contains(pos);
    }

    public List<BlockPos> getClientArea() {
        return area;
    }

    public ProfessionsSelectionType getAreaType() {
        return areaType;
    }

    public UUID getId() {
        return id;
    }
}
