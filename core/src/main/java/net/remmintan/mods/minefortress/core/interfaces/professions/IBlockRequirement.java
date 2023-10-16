package net.remmintan.mods.minefortress.core.interfaces.professions;

import net.minecraft.block.Block;

public interface IBlockRequirement {
    Block block();

    boolean blueprint();
}
