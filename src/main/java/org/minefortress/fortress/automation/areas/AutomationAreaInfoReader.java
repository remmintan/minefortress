package org.minefortress.fortress.automation.areas;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaInfo;
import net.remmintan.mods.minefortress.core.interfaces.automation.ProfessionsSelectionType;
import net.remmintan.mods.minefortress.core.interfaces.networking.INetworkingReader;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AutomationAreaInfoReader implements INetworkingReader<IAutomationAreaInfo> {
    @Override
    public IAutomationAreaInfo readBuffer(PacketByteBuf buf) {
        int size = buf.readVarInt();
        List<BlockPos> area = new ArrayList<>(size);
        for(int i = 0; i < size; i++) {
            area.add(buf.readBlockPos());
        }
        ProfessionsSelectionType name = buf.readEnumConstant(ProfessionsSelectionType.class);
        UUID id = buf.readUuid();
        return new AutomationAreaInfo(area, name, id);
    }

    @Override
    public boolean canReadForType(Class<?> type) {
        return type.isAssignableFrom(IAutomationAreaInfo.class);
    }
}
