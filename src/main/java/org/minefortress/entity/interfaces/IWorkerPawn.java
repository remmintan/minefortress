package org.minefortress.entity.interfaces;

import net.minecraft.server.world.ServerWorld;
import net.remmintan.mods.minefortress.core.interfaces.pawns.IFortressAwareEntity;
import org.minefortress.entity.ai.controls.TaskControl;

public interface IWorkerPawn extends IFortressAwareEntity, IProfessional {

    TaskControl getTaskControl();
    ServerWorld getServerWorld();

}
