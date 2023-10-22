package org.minefortress.fortress.resources.gui.craft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;

public class FortressCraftingScreenHandlerFactory implements NamedScreenHandlerFactory {
    @Override
    public Text getDisplayName() {
        return Text.literal("Crafting");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        final var fortressServer = (IFortressServer) player.getServer();
        final var serverResourceManager = fortressServer
                .get_FortressModServerManager()
                .getManagersProvider((ServerPlayerEntity) player)
                .getResourceManager();
        return new FortressCraftingScreenHandler(syncId, inv, serverResourceManager);
    }
}
