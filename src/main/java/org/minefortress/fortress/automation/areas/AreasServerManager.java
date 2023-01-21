package org.minefortress.fortress.automation.areas;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.network.c2s.S2CSyncAreasPacket;
import org.minefortress.network.helpers.FortressServerNetworkHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AreasServerManager {

    private boolean needSync = false;
    private final List<AutomationAreaInfo> areas = new ArrayList<>();

    public void addArea(AutomationAreaInfo area) {
        areas.add(area);
        sync();
    }

    public void removeArea(UUID id) {
        areas.removeIf(it -> it.id().equals(id));
        sync();
    }

    public void tick(ServerPlayerEntity serverPlayer) {
        if(serverPlayer == null) return;
        if(needSync) {
            FortressServerNetworkHelper.send(serverPlayer, S2CSyncAreasPacket.CHANNEL, new S2CSyncAreasPacket(areas));
            needSync = false;
        }
    }

    private void sync() {
        needSync = true;
    }

    public void write(NbtCompound tag) {
        var areas = new NbtCompound();
        final var nbtElements = new NbtList();
        for(AutomationAreaInfo area: this.areas) {
            nbtElements.add(toNbt(area));
        }
        areas.put("areas", nbtElements);
        tag.put("areaManager", areas);
    }

    private NbtCompound toNbt(AutomationAreaInfo automationAreaInfo) {
        var area = new NbtCompound();
        area.putUuid("id", automationAreaInfo.id());
        area.putString("areaType", automationAreaInfo.areaType().name());
        final var blocks = automationAreaInfo
                .area()
                .stream()
                .map(BlockPos::asLong)
                .toList();
        area.putLongArray("blocks", blocks);
        return area;
    }

    public void read(NbtCompound tag) {
        this.areas.clear();
        if(!tag.contains("areaManager")) return;

        var areas = tag.getCompound("areaManager");
        var nbtElements = areas.getList("areas", NbtList.COMPOUND_TYPE);
        for(int i = 0; i < nbtElements.size(); i++) {
            var area = nbtElements.getCompound(i);
            var id = area.getUuid("id");
            var areaType = ProfessionsSelectionType.valueOf(area.getString("areaType"));
            var blocks = area.getLongArray("blocks");
            var blockPosList = new ArrayList<BlockPos>();
            for(long block: blocks) {
                blockPosList.add(BlockPos.fromLong(block));
            }
            this.areas.add(new AutomationAreaInfo(blockPosList, areaType, id));
        }

        this.sync();
    }

}
