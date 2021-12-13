package org.minefortress.mixins.interfaces;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionType.class)
public interface FortressDimensionTypeMixin {

    @Accessor("THE_NETHER")
    static DimensionType getNether() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Accessor("THE_END")
    static DimensionType getEnd() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Accessor("OVERWORLD")
    static DimensionType getOverworld() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

