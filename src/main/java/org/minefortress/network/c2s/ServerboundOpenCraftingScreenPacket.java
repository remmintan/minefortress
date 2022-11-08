package org.minefortress.network.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.minefortress.fortress.resources.gui.craft.FortressCraftingScreenHandlerFactory;
import org.minefortress.fortress.resources.gui.smelt.FurnaceScreenHandlerFactory;
import org.minefortress.network.interfaces.FortressServerPacket;

public class ServerboundOpenCraftingScreenPacket implements FortressServerPacket {

    private final ScreenType screenType;
    private final BlockPos blockPos;

    public ServerboundOpenCraftingScreenPacket(ScreenType screenType) {
        this(screenType, null);
    }

    public ServerboundOpenCraftingScreenPacket(ScreenType screenType, BlockPos blockPos) {
        this.screenType = screenType;
        this.blockPos = blockPos;
    }

    public ServerboundOpenCraftingScreenPacket(PacketByteBuf buf) {
        this.screenType = ScreenType.valueOf(buf.readString());
        if (buf.readBoolean()) {
            this.blockPos = buf.readBlockPos();
        } else {
            this.blockPos = null;
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(screenType.name());
        buf.writeBoolean(blockPos != null);
        if (blockPos != null) {
            buf.writeBlockPos(blockPos);
        }
    }

    @Override
    public void handle(MinecraftServer server, ServerPlayerEntity player) {
        switch (screenType) {
            case CRAFTING -> player.openHandledScreen(new FortressCraftingScreenHandlerFactory());
            case FURNACE -> player.openHandledScreen(new FurnaceScreenHandlerFactory(blockPos));
        }

    }

    public enum ScreenType {
        FURNACE, CRAFTING
    }

}
