package net.remmintan.mods.minefortress.blocks.building

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.remmintan.mods.minefortress.core.dtos.professions.HireProgressInfo
import net.remmintan.mods.minefortress.core.dtos.professions.ProfessionHireInfo
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.buildings.IBuildingHireHandler
import net.remmintan.mods.minefortress.core.interfaces.buildings.IServerBuildingsManager
import net.remmintan.mods.minefortress.core.interfaces.professions.IServerProfessionsManager
import net.remmintan.mods.minefortress.core.interfaces.resources.IServerResourceManager
import java.util.*

class BuildingHireHandler : IBuildingHireHandler {

    private var professionManager: IServerProfessionsManager? = null
    private var buildingsManager: IServerBuildingsManager? = null
    private var resourceManger: IServerResourceManager? = null

    private var professions: List<ProfessionHireInfo> = emptyList()
    private var hireQueues = mutableMapOf<String, Queue<HireRequest>>()
    private var hireProgresses = mutableMapOf<String, HireProgressInfo>()

    fun initialized(): Boolean = professionManager != null && buildingsManager != null && resourceManger != null

    fun init(
        professionType: ProfessionType,
        professionManager: IServerProfessionsManager,
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
                    professionManager?.increaseAmount(professionId)
                }
            }
        }
    }

    override fun hire(professionId: String) {
        val hireProgress = computeHireProgress(professionId)
        if (!hireProgress.canHireMore) return

        val cost = professionManager?.getProfession(professionId)?.itemsRequirement ?: error("Profession not found")
        professionManager?.reservePawn()
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
        val hasEnoughItems = this.resourceManger?.hasItems(profession?.itemsRequirement ?: emptyList()) ?: false
        val canHireMore = (currentCount + queueLength < maxCount) && hasEnoughItems

        return HireProgressInfo(
            professionId,
            queueLength,
            currentCount,
            maxCount,
            progress,
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
        professions.map { it.professionId }.forEach {
            val hireProgress = getHireProgress(it)
            progressesTag.put(it, hireProgress.toNbt())
        }

        rootTag.put("professions", profList)
        rootTag.put("queues", queuesTag)
        rootTag.put("progresses", progressesTag)

        return rootTag
    }

    fun updateFromNbt(tag: NbtCompound) {
        val professions = tag.getList("professions", 10).map { ProfessionHireInfo.fromNbt(it as NbtCompound) }
        this.professions = professions

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
            this.hireQueues[professionId] = queue
        }

        val progressesTag = tag.getCompound("progresses")
        progressesTag.keys.forEach { professionId ->
            this.hireProgresses[professionId] =
                HireProgressInfo.fromNbt(progressesTag.getCompound(professionId))
        }
    }

    companion object {
        fun fromNbt(tag: NbtCompound): BuildingHireHandler {
            val buildingHireHandler = BuildingHireHandler()
            buildingHireHandler.updateFromNbt(tag)
            return buildingHireHandler
        }
    }

    private class HireRequest {
        var progress: Int = 0
    }

}
