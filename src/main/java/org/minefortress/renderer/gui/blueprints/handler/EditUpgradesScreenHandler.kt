package org.minefortress.renderer.gui.blueprints.handler

import net.minecraft.util.BlockRotation
import net.remmintan.mods.minefortress.core.dtos.blueprints.BlueprintSlot
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata
import net.remmintan.mods.minefortress.core.utils.CoreModUtils
import java.util.function.Consumer

class EditUpgradesScreenHandler(ids: List<String>, val edit: Consumer<BlueprintMetadata>) {

    val upgrades: List<BlueprintSlot> = ids.map {
        val blueprintManager = CoreModUtils.getBlueprintManager()
        val resourceManager = CoreModUtils.getFortressManager().resourceManager

        val blueprint = blueprintManager.blueprintMetadataManager.getByBlueprintId(it).orElseThrow()
        val blockData = blueprintManager.blockDataProvider.getBlockData(it, BlockRotation.NONE)
        val enoughResources = resourceManager.hasItems(blockData.stacks)
        BlueprintSlot(blueprint, enoughResources, blockData)
    }

}