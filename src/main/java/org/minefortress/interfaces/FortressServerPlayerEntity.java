package org.minefortress.interfaces;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.manager.ServerBlueprintManager;

public interface FortressServerPlayerEntity {

    ServerBlueprintManager getServerBlueprintManager();
    boolean wasInBlueprintWorldWhenLoggedOut();
    void setWasInBlueprintWorldWhenLoggedOut(boolean wasInBlueprintWorldWhenLoggedOut);

    @Nullable
    Vec3d getPersistedPos();
}
