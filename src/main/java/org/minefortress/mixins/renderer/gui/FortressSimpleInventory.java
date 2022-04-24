package org.minefortress.mixins.renderer.gui;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.world.GameMode;
import org.minefortress.fortress.FortressClientManager;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SimpleInventory.class)
public abstract class FortressSimpleInventory implements Inventory {
    private static final GameMode FORTRESS_GAMEMODE = ClassTinkerers.getEnum(GameMode.class, "FORTRESS");

    @Override
    public int getMaxCountPerStack() {
        if(isFortressGamemode() && isNotCreative())
            return 10000;
        else
            return Inventory.super.getMaxCountPerStack();
    }

    private boolean isFortressGamemode() {
        return getClient().interactionManager != null && FORTRESS_GAMEMODE == getClient().interactionManager.getCurrentGameMode();
    }

    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    private FortressClientManager getClientManager() {
        return ((FortressMinecraftClient) getClient()).getFortressClientManager();
    }

    private boolean isNotCreative() {
        return !getClientManager().isCreative();
    }

}
