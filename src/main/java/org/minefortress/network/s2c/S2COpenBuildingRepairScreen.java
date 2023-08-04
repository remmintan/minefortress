package org.minefortress.network.s2c;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.resources.client.ClientResourceManager;
import org.minefortress.network.interfaces.FortressS2CPacket;
import org.minefortress.renderer.gui.fortress.RepairBuildingScreen;
import org.minefortress.utils.ModUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class S2COpenBuildingRepairScreen implements FortressS2CPacket {

    public final static String CHANNEL = "open_building_repair_screen";

    private final UUID buildingId;
    private final Map<BlockPos, BlockState> blocksToRepair;

    public S2COpenBuildingRepairScreen(UUID buildingId, Map<BlockPos, BlockState> blocksToRepair) {
        this.buildingId = buildingId;
        this.blocksToRepair = blocksToRepair;
    }

    public S2COpenBuildingRepairScreen(PacketByteBuf buf) {
        this.buildingId = buf.readUuid();
        final int blocksToRepairSize = buf.readInt();
        blocksToRepair = new HashMap<>(blocksToRepairSize);
        for (int i = 0; i < blocksToRepairSize; i++) {
            final BlockPos pos = buf.readBlockPos();
            final BlockState state = Block.getStateFromRawId(buf.readInt());
            blocksToRepair.put(pos, state);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(buildingId);
        buf.writeInt(blocksToRepair.size());


        blocksToRepair.forEach((pos, state) -> {
            buf.writeBlockPos(pos);
            buf.writeInt(Block.getRawIdFromState(state));
        });

    }

    @Override
    public void handle(MinecraftClient client) {
        final var resourceManager = ModUtils.getFortressClientManager().getResourceManager();
        client.execute(() -> client.setScreen(new RepairBuildingScreen(buildingId, blocksToRepair, resourceManager)));
    }
}
