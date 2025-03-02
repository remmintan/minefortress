package org.minefortress.fortress.resources.gui.craft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.jetbrains.annotations.Nullable;

public class FortressCraftingScreenHandlerFactory implements NamedScreenHandlerFactory {
    @Override
    public Text getDisplayName() {
        return Text.literal("Crafting");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            final var serverResourceManager = ServerModUtils.getManagersProvider(serverPlayer).getResourceManager();
            return new FortressCraftingScreenHandler(syncId, inv, serverResourceManager);
        }
        return null;
    }
}
