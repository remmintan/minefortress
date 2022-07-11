package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.FortressServerManager;
import org.minefortress.interfaces.FortressServer;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundChangeMaxColonistsCountPacket implements FortressServerPacket {

    private final ActionType actionType;

    public ServerboundChangeMaxColonistsCountPacket(ActionType actionType) {
        this.actionType = actionType;
    }

    public ServerboundChangeMaxColonistsCountPacket(PacketByteBuf buf) {
        this.actionType = buf.readEnumConstant(ActionType.class);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(actionType);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressServer = (FortressServer) server;
        final var manager = fortressServer.getFortressModServerManager().getByPlayer(player);
        switch (actionType) {
            case INCREASE -> manager.increaseMaxColonistsCount();
            case DECREASE -> manager.decreaseMaxColonistsCount();
        }
    }

    public enum ActionType {
        INCREASE,
        DECREASE
    }

}
