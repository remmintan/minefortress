package net.remmintan.mods.minefortress.networking.c2s;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.ScreenType;
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket;
import org.jetbrains.annotations.NotNull;

public class ServerboundOpenCraftingScreenPacket implements FortressC2SPacket {

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
    public void handle(@NotNull MinecraftServer server, @NotNull ServerPlayerEntity player) {
        getFortressManager(player).openHandledScreen(screenType, player, blockPos);
    }

}
