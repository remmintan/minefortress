package net.remmintan.mods.minefortress.networking.s2c;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.BasePawnEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressS2CPacket;

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
        final var fortressManager = getManagersProvider().get_ClientFortressManager();

        final ClientWorld world = client.world;
        if(world == null) throw new IllegalStateException("Client world is null");
        final Entity entity = world.getEntityById(entityId);
        if(entity == null) throw new IllegalStateException("Entity with id " + entityId + " does not exist!");
        if(entity instanceof BasePawnEntity && entity instanceof LivingEntity le) {
            fortressManager.select(le);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(this.entityId);
    }
}
