package org.minefortress.mixins.renderer.gui.worldcreator;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.interfaces.IFortressGamemodeHolder;
import net.remmintan.mods.minefortress.core.interfaces.IFortressModVersionHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelProperties.class)
public abstract class LevelPropertiesMixin implements IFortressGamemodeHolder, IFortressModVersionHolder {

    private static final int CURRENT_VERSION = 3;

    @Unique
    private FortressGamemode fortressGamemode = FortressGamemode.SURVIVAL;

    @Unique
    private int forterssModVersion = CURRENT_VERSION;

    @Override
    public FortressGamemode get_fortressGamemode() {
        return fortressGamemode;
    }

    @Inject(method = "readProperties", at = @At("RETURN"), cancellable = true)
    private static <T> void readProperties(Dynamic<T> dynamic, DataFixer dataFixer, int dataVersion, NbtCompound playerData, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, LevelProperties.SpecialProperty specialProperty, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> cir) {
        final var properties = cir.getReturnValue();

        final var fortressGamemodeLabel = dynamic.get("fortressGamemode").asString("SURVIVAL");
        final var gamemode = FortressGamemode.valueOf(fortressGamemodeLabel);
        if (properties instanceof IFortressGamemodeHolder gamemodeHolder) {
            gamemodeHolder.set_fortressGamemode(gamemode);
        }

        final var fortressModVersion = dynamic.get("fortressModVersion").asInt(-1);
        if (properties instanceof IFortressModVersionHolder holder) {
            holder.set_FortressModVersion(fortressModVersion);
        }

        cir.setReturnValue(properties);
    }

    @Override
    public void set_FortressModVersion(int version) {
        this.forterssModVersion = version;
    }

    @Override
    public void set_fortressGamemode(FortressGamemode fortressGamemode) {
        this.fortressGamemode = fortressGamemode;
    }

    @Override
    public boolean is_OutdatedVersion() {
        return forterssModVersion < CURRENT_VERSION;
    }

    @Inject(method = "updateProperties", at = @At("TAIL"))
    public void storeFortressGamemode(DynamicRegistryManager registryManager, NbtCompound levelNbt, NbtCompound playerNbt, CallbackInfo ci) {
        levelNbt.putString("fortressGamemode", this.fortressGamemode.name());
        levelNbt.putInt("fortressModVersion", this.forterssModVersion);
    }

}
