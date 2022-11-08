package org.minefortress.network.s2c;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressS2CPacket;

import java.util.*;

public class ClientboundSyncSpecialBlocksPacket implements FortressS2CPacket {

    private final Map<Block, List<BlockPos>> basicSpecialBlocks;
    private final Map<Block, List<BlockPos>> blueprintSpecialBlocks;

    public ClientboundSyncSpecialBlocksPacket(Map<Block, List<BlockPos>> basicSpecialBlocks, Map<Block, List<BlockPos>> blueprintSpecialBlocks) {
        this.basicSpecialBlocks = basicSpecialBlocks;
        this.blueprintSpecialBlocks = blueprintSpecialBlocks;
    }

    public ClientboundSyncSpecialBlocksPacket(PacketByteBuf buf) {
        this.basicSpecialBlocks = getSpecialBLocks(buf);
        this.blueprintSpecialBlocks = getSpecialBLocks(buf);
    }

    @NotNull
    private HashMap<Block, List<BlockPos>> getSpecialBLocks(PacketByteBuf buf) {
        final var specialBlocks = new HashMap<Block, List<BlockPos>>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            Block block = Registry.BLOCK.get(new Identifier(buf.readString()));
            int blocksAmount = buf.readInt();
            List<BlockPos> set = specialBlocks.computeIfAbsent(block, k -> new ArrayList<>());
            for (int j = 0; j < blocksAmount; j++) {
                set.add(buf.readBlockPos());
            }
        }
        return specialBlocks;
    }

    @Override
    public void handle(MinecraftClient client) {
        final FortressMinecraftClient fortressMinecraftClient = (FortressMinecraftClient) client;
        final FortressClientManager fortressClientManager = fortressMinecraftClient.getFortressClientManager();
        fortressClientManager.setSpecialBlocks(basicSpecialBlocks, blueprintSpecialBlocks);
    }

    @Override
    public void write(PacketByteBuf buf) {
        writeSpecialBlocks(buf, this.basicSpecialBlocks);
        writeSpecialBlocks(buf, this.blueprintSpecialBlocks);
    }

    private void writeSpecialBlocks(PacketByteBuf buf, Map<Block, List<BlockPos>> basicSpecialBlocks) {
        buf.writeInt(basicSpecialBlocks.size());
        for (Map.Entry<Block, List<BlockPos>> entry : basicSpecialBlocks.entrySet()) {
            buf.writeString(Registry.BLOCK.getId(entry.getKey()).toString());
            buf.writeInt(entry.getValue().size());
            for (BlockPos pos : entry.getValue()) {
                buf.writeBlockPos(pos);
            }
        }
    }
}
