package org.minefortress.fortress.automation.areas;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.IAutomationArea;
import org.minefortress.network.c2s.S2CSyncAreasPacket;
import org.minefortress.network.helpers.FortressServerNetworkHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public final class AreasServerManager {

    private boolean needSync = false;
    private final List<ServerAutomationAreaInfo> areas = new ArrayList<>();

    public void addArea(AutomationAreaInfo area) {
        areas.add(new ServerAutomationAreaInfo(area));
        sync();
    }

    public void removeArea(UUID id) {
        areas.removeIf(it -> it.getId().equals(id));
        sync();
    }

    public void tick(ServerPlayerEntity serverPlayer) {
        if(serverPlayer == null) return;
        if(needSync) {
            final var automationAreaInfos = areas.stream().map(AutomationAreaInfo.class::cast).toList();
            FortressServerNetworkHelper.send(serverPlayer, S2CSyncAreasPacket.CHANNEL, new S2CSyncAreasPacket(automationAreaInfos));
            needSync = false;
        }
    }

    public Stream<IAutomationArea> getByRequirement(String requirement) {
        return areas.stream()
                .filter(it -> it.getAreaType().satisfies(requirement))
                .map(ServerAutomationAreaInfo.class::cast);
    }

    private void sync() {
        needSync = true;
    }

    public void write(NbtCompound tag) {
        var areas = new NbtCompound();
        final var nbtElements = new NbtList();
        for(ServerAutomationAreaInfo area: this.areas) {
            nbtElements.add(area.toNbt());
        }
        areas.put("areas", nbtElements);
        tag.put("areaManager", areas);
    }

    public void read(NbtCompound tag) {
        this.areas.clear();
        if(!tag.contains("areaManager")) return;

        var areas = tag.getCompound("areaManager");
        var nbtElements = areas.getList("areas", NbtList.COMPOUND_TYPE);
        for(int i = 0; i < nbtElements.size(); i++) {
            var areaTag = nbtElements.getCompound(i);
            final var area = ServerAutomationAreaInfo.formNbt(areaTag);
            this.areas.add(area);
        }

        this.sync();
    }

}
