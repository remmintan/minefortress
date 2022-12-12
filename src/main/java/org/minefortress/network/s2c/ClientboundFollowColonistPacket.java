package org.minefortress.network.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.network.interfaces.FortressS2CPacket;

public class ClientboundFollowColonistPacket implements FortressS2CPacket {

    private final int entityId;

    public ClientboundFollowColonistPacket(int entityId) {
        this.entityId = entityId;
    }

    public ClientboundFollowColonistPacket(PacketByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void handle(MinecraftClient client) {
        if(client instanceof FortressMinecraftClient fortressMinecraftClient) {
            final ClientWorld world = client.world;
            if(world == null) throw new NullPointerException("Client world is null");
            final Entity entity = world.getEntityById(entityId);
            if(entity == null) throw new NullPointerException("Entity with id " + entityId + " does not exist!");
            if(entity instanceof BasePawnEntity pawn) {
                fortressMinecraftClient.getFortressClientManager().select(pawn);
            } else {
                throw new IllegalArgumentException("Entity with id " + entityId + " is not a Colonist!");
            }
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.entityId);
    }
}
