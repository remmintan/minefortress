package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

public class ServerboundChangeMaxColonistsCountPacket implements FortressC2SPacket {

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
        final var manager = getFortressManager(server, player);
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
