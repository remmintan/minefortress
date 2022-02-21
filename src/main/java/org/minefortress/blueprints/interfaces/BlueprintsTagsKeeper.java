package org.minefortress.blueprints.interfaces;

import net.minecraft.nbt.NbtCompound;

public interface BlueprintsTagsKeeper {

    void setBlueprint(String blueprintFileName, NbtCompound tag);
    void removeBlueprint(String blueprintFileName);

}
