package org.minefortress.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreenHandlerFactory;
import org.minefortress.fortress.resources.gui.smelt.FurnaceScreenHandlerFactory;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundOpenCraftingScreenPacket implements FortressServerPacket {

    private final ScreenType screenType;

    public ServerboundOpenCraftingScreenPacket(ScreenType screenType) {
        this.screenType = screenType;
    }

    public ServerboundOpenCraftingScreenPacket(PacketByteBuf buf) {
        this.screenType = ScreenType.valueOf(buf.readString());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(screenType.name());
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        switch (screenType) {
            case CRAFTING -> player.openHandledScreen(new FortressCraftingScreenHandlerFactory());
            case FURNACE -> player.openHandledScreen(new FurnaceScreenHandlerFactory());
        }

    }

    public enum ScreenType {
        FURNACE, CRAFTING
    }

}
