package org.minefortress.fortress.resources.gui.smelt;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType;
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding;
import net.remmintan.mods.minefortress.core.utils.ServerModUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FurnaceScreenHandlerFactory implements NamedScreenHandlerFactory {

    private final BlockPos furnacePos;

    public FurnaceScreenHandlerFactory(BlockPos furnacePos) {
        this.furnacePos = furnacePos;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Furnace Screen Factory");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            final var provider = ServerModUtils.getManagersProvider(serverPlayer).orElseThrow();
            final var professionManager = provider.getProfessionsManager();
            final var blacksmithsCount = professionManager.getProfession("blacksmith").getAmount();
            final var otherFurnacePositions = provider
                    .getBuildingsManager()
                    .getBuildings(ProfessionType.BLACKSMITH)
                    .stream()
                    .limit(blacksmithsCount)
                    .map(IFortressBuilding::getFurnacePos)
                    .filter(Objects::nonNull)
                    .toList();

            final BlockPos selectedFurnacePos = furnacePos == null ?  otherFurnacePositions.get(0) : furnacePos;
            final var otherFurnacesDelegates = otherFurnacePositions.stream()
                    .map(it -> {
                        final var blockEnt = player.getWorld().getBlockEntity(it);
                        if (blockEnt instanceof FurnaceBlockEntity furnaceBlockEntity) {
                            return (PropertyDelegate)new FortressFurnacePropertyDelegateImpl(furnaceBlockEntity, furnaceBlockEntity.getPos().equals(selectedFurnacePos));
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            final var selectedBlockEnt = player.getWorld().getBlockEntity(selectedFurnacePos);
            if(selectedBlockEnt instanceof FurnaceBlockEntity furnaceBlockEntity) {
                final var resourceManager = provider.getResourceManager();
                return new FortressFurnaceScreenHandler(syncId, inv, resourceManager, furnaceBlockEntity, furnaceBlockEntity.propertyDelegate, otherFurnacesDelegates);
            }
        }
        return null;
    }
}
