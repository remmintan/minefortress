package org.minefortress.entity;

import net.minecraft.server.world.ServerWorld;
import org.minefortress.entity.ai.controls.TaskControl;

public interface IWorkerPawn extends IFortressAwareEntity {

    TaskControl getTaskControl();
    String getProfessionId();
    void resetProfession();
    ServerWorld getServerWorld();

}
