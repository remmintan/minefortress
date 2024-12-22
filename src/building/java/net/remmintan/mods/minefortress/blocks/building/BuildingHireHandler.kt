package net.remmintan.mods.minefortress.blocks.building

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.remmintan.mods.minefortress.core.dtos.professions.HireProgressInfo
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionHireInfo
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.buildings.IBuildingHireHandler
import net.remmintan.mods.minefortress.core.interfaces.buildings.IServerBuildingsManager
import net.remmintan.mods.minefortress.core.interfaces.professions.IProfessionsManager
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager
import java.util.*

class BuildingHireHandler : IBuildingHireHandler {

    private var professionManager: IProfessionsManager? = null
    private var buildingsManager: IServerBuildingsManager? = null
    private var resourceManger: IServerResourceManager? = null

    private var professions: List<ProfessionHireInfo> = emptyList()
    private var hireQueues = mutableMapOf<String, Queue<HireRequest>>()
    private var hireProgresses = mutableMapOf<String, HireProgressInfo>()

    fun initialized(): Boolean = professionManager != null && buildingsManager != null && resourceManger != null

    fun init(
        professionType: ProfessionType,
        professionManager: IProfessionsManager,
        buildingsManager: IServerBuildingsManager,
        resourceManager: IServerResourceManager
    ) {
        this.professionManager = professionManager
        this.buildingsManager = buildingsManager
        this.resourceManger = resourceManager

        professions = professionManager
            .getProfessionsByType(professionType)
            .filter { buildingsManager.hasRequiredBuilding(it.requirementType, it.requirementLevel, 0) }
            .map {
                ProfessionHireInfo(
                    it.id,
                    it.title,
                    it.icon,
                    it.itemsRequirement
                )
            }
    }

    fun tick() {
        hireQueues.forEach { (professionId, queue) ->
            if (queue.isNotEmpty()) {
                val request = queue.peek()
                request.progress++
                if (request.progress >= 100) {
                    queue.poll()
                    professionManager?.increaseAmount(professionId, true)
                }
            }
        }
    }

    override fun hire(professionId: String) {
        val hireProgress = computeHireProgress(professionId)
        if (!hireProgress.canHireMore) return

        val cost = professionManager?.getProfession(professionId)?.itemsRequirement ?: error("Profession not found")
        resourceManger?.removeItems(cost)

        hireQueues.computeIfAbsent(professionId) { LinkedList() }.add(HireRequest())
    }

    override fun getProfessions(): List<ProfessionHireInfo> = professions

    override fun getHireProgress(professionId: String): HireProgressInfo {
        if (buildingsManager == null) {
            // we are on client
            return hireProgresses[professionId] ?: HireProgressInfo.getEmpty(professionId)
        } else {
            val progress = computeHireProgress(professionId)
            hireProgresses[professionId] = progress
            return progress
        }
    }

    private fun computeHireProgress(professionId: String): HireProgressInfo {
        if (buildingsManager == null || professionManager == null) {
            error("The compute hire progress must be called on the server")
        }
        val profession = professionManager?.getProfession(professionId)

        val queueLength = hireQueues[professionId]?.size ?: 0
        val currentCount = profession?.amount ?: 0
        val progress = hireQueues[professionId]?.peek()?.progress ?: 0
        val maxCount = profession
            ?.let { buildingsManager?.getBuildings(it.requirementType, it.requirementLevel) }
            ?.sumOf { it.metadata.capacity } ?: 0
        val canHireMore =
            currentCount < maxCount && (this.resourceManger?.hasItems(profession?.itemsRequirement ?: emptyList())
                ?: false)

        return HireProgressInfo(
            professionId,
            queueLength,
            currentCount,
            progress,
            maxCount,
            canHireMore
        )
    }

    fun toNbt(): NbtCompound {
        val rootTag = NbtCompound()

        // professions
        val profList = NbtList().apply {
            professions.forEach { add(it.toNbt()) }
        }

        val queuesTag = NbtCompound()
        hireQueues.forEach { (professionId, queue) ->
            val queueTag = NbtCompound()
            queue.forEachIndexed { index, request ->
                queueTag.put("$index", NbtCompound().apply {
                    putInt("progress", request.progress)
                })
            }
            queuesTag.put(professionId, queueTag)
        }

        val progressesTag = NbtCompound()
        hireProgresses.forEach { (professionId, progress) ->
            progressesTag.put(professionId, progress.toNbt())
        }

        rootTag.put("professions", profList)
        rootTag.put("queues", queuesTag)
        rootTag.put("progresses", progressesTag)

        return rootTag
    }

    companion object {
        fun fromNbt(tag: NbtCompound): BuildingHireHandler {
            val buildingHireHandler = BuildingHireHandler()

            val professions = tag.getList("professions", 10).map { ProfessionHireInfo.fromNbt(it as NbtCompound) }
            buildingHireHandler.professions = professions

            val queuesTag = tag.getCompound("queues")
            queuesTag.keys.forEach { professionId ->
                val queueTag = queuesTag.getCompound(professionId)
                val queue = LinkedList<HireRequest>()
                queueTag.keys.forEach { index ->
                    val requestTag = queueTag.getCompound(index)
                    val request = HireRequest()
                    request.progress = requestTag.getInt("progress")
                    queue.add(request)
                }
                buildingHireHandler.hireQueues[professionId] = queue
            }

            val progressesTag = tag.getCompound("progresses")
            progressesTag.keys.forEach { professionId ->
                buildingHireHandler.hireProgresses[professionId] =
                    HireProgressInfo.fromNbt(progressesTag.getCompound(professionId))
            }

            return buildingHireHandler
        }
    }

    private class HireRequest {
        var progress: Int = 0
    }

}