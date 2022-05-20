package org.minefortress.fortress.resources.gui.smelt;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.minefortress.interfaces.FortressServerPlayerEntity;

import java.util.Objects;

public class FurnaceScreenHandlerFactory implements NamedScreenHandlerFactory {
    @Override
    public Text getDisplayName() {
        return new LiteralText("Furnace Screen Factory");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        if(player instanceof FortressServerPlayerEntity fortressServerPlayer) {
            final var manager = fortressServerPlayer.getFortressServerManager();
            final var furnaceBlockOpt = manager.getSpecialBlocksByType(Blocks.FURNACE, true)
                    .stream()
                    .filter(Objects::nonNull)
                    .findFirst();
            if(furnaceBlockOpt.isPresent()) {
                final var furnaceBlockPos = furnaceBlockOpt.get();
                    final var blockEntity = player.world.getBlockEntity(furnaceBlockPos);
                    if(blockEntity instanceof FurnaceBlockEntity furnaceBlockEntity) {
                        final var resourceManager = manager.getServerResourceManager();
                        return new FortressFurnaceScreenHandler(syncId, inv, resourceManager, furnaceBlockEntity, furnaceBlockEntity.propertyDelegate);
                    }
            }
        }
        return null;
    }
}
