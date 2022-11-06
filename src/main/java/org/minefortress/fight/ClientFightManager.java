package org.minefortress.fight;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.minefortress.entity.Colonist;
import org.minefortress.fortress.FortressClientManager;

import java.util.function.Supplier;

public class ClientFightManager {

    private final ClientFightSelectionManager selectionManager;
    private final Supplier<FortressClientManager> fortressClientManagerSupplier;

    public ClientFightManager(Supplier<FortressClientManager> fortressClientManagerSupplier) {
         selectionManager = new ClientFightSelectionManager(fortressClientManagerSupplier);
        this.fortressClientManagerSupplier = fortressClientManagerSupplier;
    }

    public ClientFightSelectionManager getSelectionManager() {
        return selectionManager;
    }

    public void setTarget(HitResult hitResult) {
        if(hitResult instanceof BlockHitResult blockHitResult) {
            final var blockPos = blockHitResult.getBlockPos();
            final var mainHandStack = MinecraftClient.getInstance().player.getMainHandStack();
// select fight target
        } else if (hitResult instanceof EntityHitResult entityHitResult) {
            final var entity = entityHitResult.getEntity();
            if(!(entity instanceof LivingEntity livingEntity)) return;
            if(entity instanceof Colonist col) {
                final var colonistFortressId = col.getFortressId();
                if(colonistFortressId != null && colonistFortressId.equals(fortressClientManagerSupplier.get().getId()))
                    return;
            }
// select fight target
        }
    }

    public void setTarget(Entity entity) {
        if(!(entity instanceof LivingEntity livingEntity)) return;
        if(entity instanceof Colonist col) {
            final var colonistFortressId = col.getFortressId();
            if(colonistFortressId != null && colonistFortressId.equals(fortressClientManagerSupplier.get().getId()))
                return;
        }
// select fight target
    }
}
