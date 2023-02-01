package org.minefortress.fortress.automation.areas;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.minefortress.fortress.IAutomationArea;
import org.minefortress.fortress.automation.AutomationBlockInfo;
import org.minefortress.fortress.automation.iterators.FarmAreaIterator;
import org.minefortress.fortress.automation.iterators.ResetableIterator;
import org.minefortress.utils.AreasUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public final class ServerAutomationAreaInfo extends AutomationAreaInfo implements IAutomationArea {

    private LocalDateTime updated = LocalDateTime.MIN;
    private ResetableIterator<AutomationBlockInfo> currentIterator;
    private boolean reset = false;

    public ServerAutomationAreaInfo(AutomationAreaInfo info) {
        super(info.getArea(), info.getAreaType(), info.getId());
    }

    private ServerAutomationAreaInfo(List<BlockPos> area, ProfessionsSelectionType areaType, UUID id, LocalDateTime updated) {
        super(area, areaType, id);
        this.updated = updated;
    }

    @Override
    public Iterator<AutomationBlockInfo> iterator(World world) {
        if(this.reset) {
            return Collections.emptyIterator();
        }
        if(currentIterator == null || !currentIterator.hasNext())
            this.currentIterator = new FarmAreaIterator(this.getArea(), world);
        return currentIterator;
    }

    public void refresh(World world) {
        final var area = this.getArea();
        if(area.isEmpty()) return;
        final var first = area.get(0);
        final var flatBlocks = area.stream().map(it -> it.withY(first.getY())).collect(Collectors.toSet());
        super.area = AreasUtils.buildAnAreaOnSurfaceWithinBlocks(flatBlocks, world);
    }

    @Override
    public void update() {
        updated = LocalDateTime.now();
    }

    @Override
    public LocalDateTime getUpdated() {
        return updated;
    }

    public void reset() {
        if(this.currentIterator != null)
            this.currentIterator.reset();
        this.reset = true;
    }

    public NbtCompound toNbt() {
        NbtCompound tag = new NbtCompound();
        tag.putUuid("id", getId());
        tag.putLongArray("blocks", getArea().stream().map(BlockPos::asLong).toList());
        tag.putString("areaType", getAreaType().name());
        tag.putString("updated", updated.toString());
        return tag;
    }

    public static ServerAutomationAreaInfo formNbt(NbtCompound tag) {
        var id = tag.getUuid("id");
        var areaType = ProfessionsSelectionType.valueOf(tag.getString("areaType"));
        var blocks = tag.getLongArray("blocks");
        var blockPosList = Arrays.stream(blocks).mapToObj(BlockPos::fromLong).toList();
        if(tag.contains("updated")) {
            var updated = LocalDateTime.parse(tag.getString("updated"));
            return new ServerAutomationAreaInfo(blockPosList, areaType, id, updated);
        } else {
            return new ServerAutomationAreaInfo(blockPosList, areaType, id, LocalDateTime.MIN);
        }
    }

}
