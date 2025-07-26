package net.remmintan.mods.minefortress.gui.building.handlers

import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockBox
import net.remmintan.mods.minefortress.core.dtos.blueprints.BlueprintSlot
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.networking.c2s.C2SDestroyBuilding
import net.remmintan.mods.minefortress.networking.c2s.C2SRepairBuilding
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper

class InfoTabHandler(provider: IBuildingProvider) : IInfoTabHandler {

    private val building by lazy { provider.building }

    private var infoTabState = InfoTabState.TABS

    override fun getInfoTabState(): InfoTabState {
        return infoTabState
    }

    override val upgrades: List<BlueprintSlot> by lazy {
        val blueprintManager = ClientModUtils.getBlueprintManager()
        val resourceManager = ClientModUtils.getFortressManager().resourceManager
        building.upgrades
            .map { blueprintManager.blueprintMetadataManager.getByBlueprintId(it) }
            .filter { it.isPresent }
            .map { it.get() }
            .map { metadata ->
                val blockData = blueprintManager.blockDataProvider.getBlockData(metadata.id, BlockRotation.NONE)
                val theNextLevel = metadata.requirement.level == building.metadata.requirement.level + 1
                val enoughResources = resourceManager.hasItems(blockData.stacks)
                BlueprintSlot(metadata, theNextLevel && enoughResources, blockData)
            }
    }


    override fun getBlueprintMetadata(): BlueprintMetadata = building.metadata
    override fun getHealth() = building.health
    override fun getItemsToRepair(): List<ItemStack> = building.repairItemInfos

    override fun getEnoughItems(): Map<ItemStack, Boolean> {
        val itemsToRepair = getItemsToRepair().sortedBy { it.item.toString() }
        val resourceHelper = ClientModUtils.getFortressManager().resourceHelper
        return resourceHelper.getMetRequirements(itemsToRepair)
    }

    override fun upgrade(slot: BlueprintSlot) {
        val buildingBox = BlockBox.create(building.start, building.end)
        ClientModUtils.getBlueprintManager().selectToUpgrade(slot.metadata, buildingBox, building.pos)
        ClientModUtils.getClientPlayer().closeScreen()
    }

    override fun canDestroy(): Boolean {
        return building.metadata.id != "campfire"
    }

    override fun destroy() {
        if (!canDestroy()) return
        if (infoTabState == InfoTabState.DESTROY) {
            val packet = C2SDestroyBuilding(building.pos)
            FortressClientNetworkHelper.send(C2SDestroyBuilding.CHANNEL, packet)
            MinecraftClient.getInstance().setScreen(null)
        } else {
            infoTabState = InfoTabState.DESTROY
        }
    }

    override fun repair() {
        if (infoTabState == InfoTabState.REPAIR) {
            val selectedPawns = ClientModUtils.getPawnsSelectionManager().selectedPawnsIds
            val packet = C2SRepairBuilding(building.pos, selectedPawns)
            FortressClientNetworkHelper.send(C2SRepairBuilding.CHANNEL, packet)
            MinecraftClient.getInstance().setScreen(null)
        } else {
            infoTabState = InfoTabState.REPAIR
        }
    }

    override fun cancel() {
        infoTabState = InfoTabState.TABS
    }

}