package org.minefortress.fortress.automation.areas;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.automation.iterators.ResetableIterator;
import net.remmintan.mods.minefortress.core.interfaces.automation.IAutomationAreaInfo;
import net.remmintan.mods.minefortress.core.interfaces.automation.ProfessionsSelectionType;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea;
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo;
import org.minefortress.fortress.automation.iterators.FarmAreaIterator;
import org.minefortress.fortress.automation.iterators.LoggingAreaIterator;
import org.minefortress.fortress.automation.iterators.MineAreaIterator;
import org.minefortress.utils.AreasUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ServerAutomationAreaInfo extends AutomationAreaInfo implements IAutomationArea {

    private LocalDateTime updated;
    private ResetableIterator<IAutomationBlockInfo> currentIterator;
    private boolean reset = false;

    public ServerAutomationAreaInfo(IAutomationAreaInfo info) {
        this(info.getClientArea(), info.getAreaType(), info.getId(), LocalDateTime.MIN);
    }

    private ServerAutomationAreaInfo(List<BlockPos> area, ProfessionsSelectionType areaType, UUID id, LocalDateTime updated) {
        super(
            area
                .stream()
                .sorted(
                    Comparator.comparingInt(BlockPos::getY)
                        .reversed()
                        .thenComparingInt(BlockPos::getX)
                        .thenComparingInt(BlockPos::getZ)
                ).toList(),
            areaType,
            id
        );
        this.updated = updated;
    }

    @Override
    public Iterator<IAutomationBlockInfo> iterator(World world) {
        if(this.reset) {
            return Collections.emptyIterator();
        }
        if(currentIterator == null || !currentIterator.hasNext()){
            this.currentIterator = switch (getAreaType()) {
                case FARMING -> new FarmAreaIterator(this.getServerArea(), world);
                case QUARRY -> new MineAreaIterator(this.getServerArea(), world);
                case LOGGING -> new LoggingAreaIterator(this.getServerArea(), world);
            };
        }

        return currentIterator;
    }

    @Override
    public void sendFinishMessage(Consumer<String> messageConsumer) {
        if(getAreaType() == ProfessionsSelectionType.QUARRY) {
            messageConsumer.accept("Mining area is finished!");
        }
    }

    public void refresh(World world) {
        final var area = this.getClientArea();
        super.area = getRefreshedArea(world, area);
    }

    private List<BlockPos> getRefreshedArea(World world, List<BlockPos> area) {
        if (area.isEmpty()) return area;
        final var first = area.get(0);
        final var flatBlocks = area.stream().map(it -> it.withY(first.getY())).collect(Collectors.toSet());
        return AreasUtils.buildAnAreaOnSurfaceWithinBlocks(flatBlocks, world,
                super.getAreaType() == ProfessionsSelectionType.QUARRY ? Heightmap.Type.WORLD_SURFACE : Heightmap.Type.MOTION_BLOCKING);
    }

    public List<BlockPos> getServerArea() {
        return getClientArea();
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
        tag.putLongArray("blocks", getClientArea().stream().map(BlockPos::asLong).toList());
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
