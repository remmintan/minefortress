package net.remmintan.mods.minefortress.core.interfaces.entities.player;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface FortressServerPlayerEntity {

    boolean was_InBlueprintWorldWhenLoggedOut();
    void set_WasInBlueprintWorldWhenLoggedOut(boolean wasInBlueprintWorldWhenLoggedOut);

    @Nullable
    Vec3d get_PersistedPos();
}
