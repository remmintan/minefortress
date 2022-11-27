package org.minefortress.entity.interfaces;

import net.minecraft.server.world.ServerWorld;
import org.minefortress.entity.ai.controls.TaskControl;

public interface IWorkerPawn extends IFortressAwareEntity, IProfessional {

    TaskControl getTaskControl();
    ServerWorld getServerWorld();

}
