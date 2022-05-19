package org.minefortress.network;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;

import java.util.*;

public class ClientboundSyncSpecialBlocksPacket implements FortressClientPacket {

    private final Map<Block, Set<BlockPos>> basicSpecialBlocks;
    private final Map<Block, Set<BlockPos>> blueprintSpecialBlocks;

    public ClientboundSyncSpecialBlocksPacket(Map<Block, Set<BlockPos>> basicSpecialBlocks, Map<Block, Set<BlockPos>> blueprintSpecialBlocks) {
        this.basicSpecialBlocks = basicSpecialBlocks;
        this.blueprintSpecialBlocks = blueprintSpecialBlocks;
    }

    public ClientboundSyncSpecialBlocksPacket(PacketByteBuf buf) {
        this.basicSpecialBlocks = getSpecialBLocks(buf);
        this.blueprintSpecialBlocks = getSpecialBLocks(buf);
    }

    @NotNull
    private HashMap<Block, Set<BlockPos>> getSpecialBLocks(PacketByteBuf buf) {
        final var specialBlocks = new HashMap<Block, Set<BlockPos>>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            Block block = Registry.BLOCK.get(new Identifier(buf.readString()));
            int blocksAmount = buf.readInt();
            Set<BlockPos> set = specialBlocks.computeIfAbsent(block, k -> new HashSet<>());
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
        buf.writeInt(basicSpecialBlocks.size());
        for (Map.Entry<Block, Set<BlockPos>> entry : basicSpecialBlocks.entrySet()) {
            buf.writeString(Registry.BLOCK.getId(entry.getKey()).toString());
            buf.writeInt(entry.getValue().size());
            for (BlockPos pos : entry.getValue()) {
                buf.writeBlockPos(pos);
            }
        }
    }
}
