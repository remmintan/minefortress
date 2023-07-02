package org.minefortress.mixins.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.minefortress.entity.ai.goal.hostile.AttackBuildingGoal;
import org.minefortress.interfaces.FortressServer;
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
        if(world.random.nextBoolean()) return;
        if(this.getServer() instanceof FortressServer frtressServer) {
            final var modServerManager = frtressServer.getFortressModServerManager();
            final var zombie = (ZombieEntity) (Object) this;
            this.goalSelector.add(1, new AttackBuildingGoal(zombie, modServerManager));
        }
    }

}
