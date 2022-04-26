package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.resources.craft.FortressCraftingScreenHandlerFactory;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundOpenCraftingScreenPacket implements FortressServerPacket {

    public ServerboundOpenCraftingScreenPacket() {
    }

    public ServerboundOpenCraftingScreenPacket(PacketByteBuf buf) {
    }

    @Override
    public void write(PacketByteBuf buf) {}

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        player.openHandledScreen(new FortressCraftingScreenHandlerFactory());
    }

}
