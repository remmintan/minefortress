package net.remmintan.mods.minefortress.core.interfaces.blueprints.world

import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.World

val BLUEPRINT_DIMENSION_KEY: RegistryKey<World> =
    RegistryKey.of(RegistryKeys.WORLD, Identifier("minefortress", "blueprint_dimension"))