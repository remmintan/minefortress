package net.remmintan.mods.minefortress.core.interfaces.entities.pawns;

import net.minecraft.server.world.ServerWorld;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.ITaskControl;

public interface IWorkerPawn extends IFortressAwareEntity, IProfessional {

    ITaskControl getTaskControl();
    ServerWorld getServerWorld();

}
