package org.minefortress.network;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressClientPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientboundSyncSpecialBlocksPacket implements FortressClientPacket {

    private Map<Block, List<BlockPos>> specialBlocks = new HashMap<>();

    public ClientboundSyncSpecialBlocksPacket(Map<Block, List<BlockPos>> specialBlocks){
        this.specialBlocks = specialBlocks;
    }

    public ClientboundSyncSpecialBlocksPacket(PacketByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            Block block = Registry.BLOCK.get(new Identifier(buf.readString()));
            int size2 = buf.readInt();
            List<BlockPos> list = specialBlocks.computeIfAbsent(block, k -> new java.util.ArrayList<>());
            for (int j = 0; j < size2; j++) {
                list.add(buf.readBlockPos());
            }
        }
    }

    @Override
    public void handle(MinecraftClient client) {
        final FortressMinecraftClient fortressMinecraftClient = (FortressMinecraftClient) client;
        final FortressClientManager fortressClientManager = fortressMinecraftClient.getFortressClientManager();
        fortressClientManager.setSpecialBlocks(specialBlocks);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(specialBlocks.size());
        for (Map.Entry<Block, List<BlockPos>> entry : specialBlocks.entrySet()) {
            buf.writeString(Registry.BLOCK.getId(entry.getKey()).toString());
            buf.writeInt(entry.getValue().size());
            for (BlockPos pos : entry.getValue()) {
                buf.writeBlockPos(pos);
            }
        }
    }
}
