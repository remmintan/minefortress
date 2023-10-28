package org.minefortress.mixins.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.minefortress.entity.ai.goal.hostile.AttackBuildingGoal;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
public class FortressZombieEntityMixin extends HostileEntity {

    protected FortressZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initCustomGoals", at = @At("TAIL"))
    protected void initCustomGoals(CallbackInfo ci) {
        if(getWorld().random.nextFloat() >= 0.33f) return;
        if(this.getServer() instanceof IFortressServer frtressServer) {
            final var modServerManager = frtressServer.get_FortressModServerManager();
            final var zombie = (ZombieEntity) (Object) this;
            this.goalSelector.add(1, new AttackBuildingGoal(zombie, modServerManager));
        }
    }

}
