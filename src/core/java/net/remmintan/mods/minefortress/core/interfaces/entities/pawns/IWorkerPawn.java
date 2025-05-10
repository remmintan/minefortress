package net.remmintan.mods.minefortress.core.interfaces.entities.pawns;

import net.minecraft.server.world.ServerWorld;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.IAreaBasedTaskControl;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.controls.ITaskControl;

public interface IWorkerPawn extends IFortressAwareEntity, IProfessional {

    IAreaBasedTaskControl getAreaBasedTaskControl();
    ITaskControl getTaskControl();
    ServerWorld getServerWorld();

}
