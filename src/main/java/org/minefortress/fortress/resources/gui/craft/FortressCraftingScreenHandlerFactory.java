package org.minefortress.fortress.resources.gui.craft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.minefortress.interfaces.FortressServerPlayerEntity;

public class FortressCraftingScreenHandlerFactory implements NamedScreenHandlerFactory {
    @Override
    public Text getDisplayName() {
        return new LiteralText("Crafting");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        FortressServerPlayerEntity fortressPlayer = (FortressServerPlayerEntity) player;
        final var serverResourceManager = fortressPlayer.getFortressServerManager().getServerResourceManager();
        return new FortressCraftingScreenHandler(syncId, inv, serverResourceManager);
    }
}
