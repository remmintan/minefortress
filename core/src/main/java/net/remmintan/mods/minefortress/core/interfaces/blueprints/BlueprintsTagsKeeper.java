package net.remmintan.mods.minefortress.core.interfaces.blueprints;

import net.minecraft.nbt.NbtCompound;

public interface BlueprintsTagsKeeper {

    void setBlueprint(String blueprintFileName, NbtCompound tag);
    void removeBlueprint(String blueprintFileName);

}
