package org.minefortress.fortress.resources.gui.craft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.minefortress.interfaces.FortressServer;

public class FortressCraftingScreenHandlerFactory implements NamedScreenHandlerFactory {
    @Override
    public Text getDisplayName() {
        return Text.literal("Crafting");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        final var fortressServer = (FortressServer) player.getServer();
        final var serverResourceManager = fortressServer.get_FortressModServerManager().getByPlayer((ServerPlayerEntity) player).getServerResourceManager();
        return new FortressCraftingScreenHandler(syncId, inv, serverResourceManager);
    }
}
