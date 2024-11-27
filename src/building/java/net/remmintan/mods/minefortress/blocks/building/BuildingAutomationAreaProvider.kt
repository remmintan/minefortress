package net.remmintan.mods.minefortress.blocks.building

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintRequirement
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import java.time.LocalDateTime
import java.util.*

class BuildingAutomationAreaProvider(
    private val id: UUID,
    private val start: BlockPos,
    private val end: BlockPos,
    private val requirement: IBlueprintRequirement
) : IAutomationArea {

    private var updated: LocalDateTime = LocalDateTime.MIN
    private var currentIterator: Iterator<IAutomationBlockInfo>? = null

    override fun getId(): UUID = id

    override fun iterator(world: World?): Iterator<IAutomationBlockInfo> {
        if (currentIterator?.hasNext() != true) {
            if (requirement.satisfies(ProfessionType.FARMER, 0)) {
                this.currentIterator = FarmBuildingIterator(start, end, world)
            }
        }

        return currentIterator ?: error("No iterator available")
    }

    override fun update() {
        updated = LocalDateTime.now()
    }

    override fun getUpdated(): LocalDateTime = updated
}