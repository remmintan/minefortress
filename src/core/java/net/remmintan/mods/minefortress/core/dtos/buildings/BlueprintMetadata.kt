package net.remmintan.mods.minefortress.core.dtos.buildings

import net.minecraft.nbt.NbtCompound
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement

class BlueprintMetadata(
    val name: String,
    val id: String,
    var floorLevel: Int,
    val capacity: Int,
    val group: BlueprintGroup
) {

    constructor(tag: NbtCompound) : this(
        tag.getString("name"),
        tag.getString("id"),
        tag.getInt("floorLevel"),
        tag.getInt("capacity"),
        BlueprintGroup.valueOf(tag.getString("group"))
    )

    val requirement = BlueprintRequirement(id) as IBlueprintRequirement

    fun toNbt() = NbtCompound().apply {
        putString("name", name)
        putString("id", id)
        putInt("floorLevel", floorLevel)
        putInt("capacity", capacity)
        putString("group", group.name)
    }

}
