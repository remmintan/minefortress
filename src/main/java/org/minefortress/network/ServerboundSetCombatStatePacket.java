package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.interfaces.FortressServerPlayerEntity;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundSetCombatStatePacket implements FortressServerPacket {

    private final boolean combatMode;

    public ServerboundSetCombatStatePacket(boolean combatMode) {
        this.combatMode = combatMode;
    }

    public ServerboundSetCombatStatePacket(PacketByteBuf buf) {
        this.combatMode = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(combatMode);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(player instanceof FortressServerPlayerEntity fortressPlayer) {
            fortressPlayer.getFortressServerManager().setCombatMode(combatMode);
        }
    }
}
