package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class C2SRepairBuilding implements FortressC2SPacket {

    public static final String CHANNEL = "repair_building";

    private final List<Integer> selectedPawns;
    private final BlockPos pos;

    public C2SRepairBuilding(BlockPos pos, List<Integer> selectedPawns) {
        this.pos = pos;
        this.selectedPawns = selectedPawns;
    }

    public C2SRepairBuilding(PacketByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        selectedPawns = new ArrayList<>();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            selectedPawns.add(buf.readInt());
        }
    }


    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var serverManager = getFortressManager(server, player);
        serverManager.repairBuilding(player, pos, selectedPawns);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(pos.asLong());
        buf.writeInt(selectedPawns.size());
        for (Integer selectedPawn : selectedPawns) {
            buf.writeInt(selectedPawn);
        }
    }
}
