package org.minefortress.mixins;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.remmintan.mods.minefortress.core.FortressGamemode;
import net.remmintan.mods.minefortress.core.interfaces.IFortressGamemodeHolder;
import net.remmintan.mods.minefortress.core.interfaces.blueprints.world.IBlueprintWorld;
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer;
import org.jetbrains.annotations.Nullable;
import org.minefortress.blueprints.world.BlueprintWorldWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class IFortressServerMixin extends ReentrantThreadExecutor<ServerTask> implements IFortressServer {

    @Shadow
    @Nullable
    public abstract ServerWorld getWorld(RegistryKey<World> key);

    @Shadow
    public abstract SaveProperties getSaveProperties();

    public IFortressServerMixin(String string) {
        super(string);
    }

    @Override
    public FortressGamemode get_FortressGamemode() {
        final var saveProperties = getSaveProperties();
        if (saveProperties instanceof IFortressGamemodeHolder holder) {
            return holder.get_fortressGamemode();
        } else {
            throw new IllegalStateException("SaveProperties is not a IFortressGamemodeHolder");
        }
    }

    @Override
    public IBlueprintWorld get_BlueprintWorld() {
        return new BlueprintWorldWrapper((MinecraftServer) (Object) this);
    }
}
