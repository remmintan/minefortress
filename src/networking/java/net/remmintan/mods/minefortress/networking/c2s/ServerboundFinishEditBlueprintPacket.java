package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.interfaces.entities.player.FortressServerPlayerEntity;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;

public class ServerboundFinishEditBlueprintPacket implements FortressC2SPacket {

    private final boolean shouldSave;

    public ServerboundFinishEditBlueprintPacket(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public ServerboundFinishEditBlueprintPacket(PacketByteBuf buf) {
        this.shouldSave = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(this.shouldSave);
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        if(shouldSave) {
            updateBlueprint(server, player);
        }

        final ServerWorld world = server.getWorld(World.OVERWORLD);
        player.getInventory().clear();
        player.moveToWorld(world);
    }

    private void updateBlueprint(MinecraftServer server, ServerPlayerEntity player) {
        final var fortressPlayer = (FortressServerPlayerEntity) player;
        fortressPlayer.get_ServerBlueprintManager().finishBlueprintEdit(shouldSave, server, player);
    }
}
