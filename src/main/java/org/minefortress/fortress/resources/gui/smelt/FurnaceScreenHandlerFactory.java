package org.minefortress.fortress.resources.gui.smelt;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.minefortress.interfaces.FortressServerPlayerEntity;

import java.util.Objects;

public class FurnaceScreenHandlerFactory implements NamedScreenHandlerFactory {

    private final BlockPos furnacePos;

    public FurnaceScreenHandlerFactory(BlockPos furnacePos) {
        this.furnacePos = furnacePos;
    }

    @Override
    public Text getDisplayName() {
        return new LiteralText("Furnace Screen Factory");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        if(player instanceof FortressServerPlayerEntity fortressServerPlayer) {
            final var manager = fortressServerPlayer.getFortressServerManager();
            final var professionManager = manager.getServerProfessionManager();
            final var blacksmithsCount = professionManager.getProfession("blacksmith").getAmount();
            final var otherFurnaceBlocks = manager.getSpecialBlocksByType(Blocks.FURNACE, true)
                    .stream()
                    .limit(blacksmithsCount)
                    .filter(Objects::nonNull)
                    .toList();

            final BlockPos selectedFurnacePos = furnacePos == null ? otherFurnaceBlocks.get(0) : furnacePos;
            final var otherFurnacesDelegates = otherFurnaceBlocks.stream()
                    .map(it -> {
                        final var blockEnt = player.world.getBlockEntity(it);
                        if (blockEnt instanceof FurnaceBlockEntity furnaceBlockEntity) {
                            return (PropertyDelegate)new FortressFurnacePropertyDelegateImpl(furnaceBlockEntity, furnaceBlockEntity.getPos().equals(selectedFurnacePos));
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();


            final var selectedBlockEnt = player.world.getBlockEntity(selectedFurnacePos);
            if(selectedBlockEnt instanceof FurnaceBlockEntity furnaceBlockEntity) {
                final var resourceManager = manager.getServerResourceManager();
                return new FortressFurnaceScreenHandler(syncId, inv, resourceManager, furnaceBlockEntity, furnaceBlockEntity.propertyDelegate, otherFurnacesDelegates);
            }
        }
        return null;
    }
}
