package org.minefortress.interfaces;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.manager.ServerBlueprintManager;

public interface FortressServerPlayerEntity {

    ServerBlueprintManager get_ServerBlueprintManager();
    boolean was_InBlueprintWorldWhenLoggedOut();
    void set_WasInBlueprintWorldWhenLoggedOut(boolean wasInBlueprintWorldWhenLoggedOut);

    @Nullable
    Vec3d get_PersistedPos();
}
