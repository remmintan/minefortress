package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Block block = Registries.BLOCK.get(new Identifier(buf.readString()));
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
        final var provider = getManagersProvider();
        final var manager = provider.get_ClientFortressManager();
        manager.setSpecialBlocks(basicSpecialBlocks, blueprintSpecialBlocks);
    }

    @Override
    public void write(PacketByteBuf buf) {
        writeSpecialBlocks(buf, this.basicSpecialBlocks);
        writeSpecialBlocks(buf, this.blueprintSpecialBlocks);
    }

    private void writeSpecialBlocks(PacketByteBuf buf, Map<Block, List<BlockPos>> basicSpecialBlocks) {
        buf.writeInt(basicSpecialBlocks.size());
        for (Map.Entry<Block, List<BlockPos>> entry : basicSpecialBlocks.entrySet()) {
            buf.writeString(Registries.BLOCK.getId(entry.getKey()).toString());
            buf.writeInt(entry.getValue().size());
            for (BlockPos pos : entry.getValue()) {
                buf.writeBlockPos(pos);
            }
        }
    }
}
