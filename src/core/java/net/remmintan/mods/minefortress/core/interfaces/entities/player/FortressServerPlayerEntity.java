package net.remmintan.mods.minefortress.core.interfaces.entities.player;

import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IServerBlueprintManager;
import org.jetbrains.annotations.Nullable;

public interface FortressServerPlayerEntity {

    IServerBlueprintManager get_ServerBlueprintManager();
    boolean was_InBlueprintWorldWhenLoggedOut();
    void set_WasInBlueprintWorldWhenLoggedOut(boolean wasInBlueprintWorldWhenLoggedOut);

    @Nullable
    Vec3d get_PersistedPos();
}
