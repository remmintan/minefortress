package net.remmintan.mods.minefortress.blocks.building

import net.minecraft.block.BedBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.tag.BlockTags
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.World
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.core.dtos.ItemInfo
import net.remmintan.mods.minefortress.core.dtos.buildings.BarColor
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata
import net.remmintan.mods.minefortress.core.dtos.buildings.HudBar
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.core.utils.getManagersProvider
import net.remmintan.mods.minefortress.gui.building.BuildingScreenHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.asSequence

private const val MAX_BLOCKS_PER_UPDATE = 10
private val LOGGER: Logger = LoggerFactory.getLogger(FortressBuildingBlockEntity::class.java)

class FortressBuildingBlockEntity(pos: BlockPos?, state: BlockState?) :
    BlockEntity(FortressBlocks.BUILDING_ENT_TYPE, pos, state),
    NamedScreenHandlerFactory,
    IFortressBuilding {

    private var fortressPos: BlockPos? = null
    private var blueprintMetadata: BlueprintMetadata? = null
    private var start: BlockPos? = null
    private var end: BlockPos? = null
    private var blockData: FortressBuildingBlockData? = null
    private var furnaceBlockPositions: List<BlockPos>? = null
    private var hireHandler: BuildingHireHandler = BuildingHireHandler()
    private val attackers: MutableSet<HostileEntity> = HashSet()
    var selectedTabIndex = 0

    private val automationArea: IAutomationArea by lazy {
        BuildingAutomationArea(start!!, end!!, metadata.requirement)
    }

    override fun getAutomationArea(): Optional<IAutomationArea> {
        return Optional.ofNullable(
            if (start != null && end != null) {
                automationArea
            } else {
                null
            }
        )
    }

    fun init(
        fortressPos: BlockPos,
        metadata: BlueprintMetadata,
        start: BlockPos,
        end: BlockPos,
        blockData: Map<BlockPos, BlockState>,
    ) {
        this.fortressPos = fortressPos
        this.blueprintMetadata = metadata
        this.start = start
        this.end = end

        val movedBlocksData = blockData.mapKeys { it.key.add(start) }

        this.blockData = FortressBuildingBlockData(movedBlocksData, metadata.floorLevel)

        this.findFurnaces()

        this.hireHandler = BuildingHireHandler()
    }

    fun tick(world: World?) {
        world ?: return
        blockData?.checkTheNextBlocksState(MAX_BLOCKS_PER_UPDATE, world as? ServerWorld)

        hireHandler.let {
            if (!it.initialized()) {
                val professionType = metadata.requirement.type ?: return@let

                if (world is ServerWorld) {
                    val provider = world.server.getManagersProvider(fortressPos!!)
                    checkNotNull(provider)

                    it.init(
                        professionType,
                        provider.professionsManager,
                        provider.buildingsManager,
                        provider.resourceManager,
                        provider.resourceHelper
                    )
                }
            }

            if (it.initialized())
                it.tick()
        }

        this.markDirty()
        if (this.world?.isClient == false) {
            val state = this.cachedState
            this.world?.updateListeners(this.pos, state, state, Block.NOTIFY_ALL)
        }
    }

    override fun getRandomPosToComeToBuilding(): BlockPos? {
        val world = world ?: return null
        val start = start ?: return null
        val end = end ?: return null

        val startY = min(start.y + metadata.floorLevel, end.y)
        val endY = max(start.y + metadata.floorLevel, end.y)

        var attempts = 0
        do {
            val randPos = BlockPos.iterateRandomly(world.random, 1, pos, 10)
                .map {
                    val y = world.getTopY(Heightmap.Type.WORLD_SURFACE, it.x, it.z)
                    BlockPos(it.x, y, it.z)
                }
                .first()


            val probablyOnTheRoof = endY - startY > 3 && abs(randPos.y - startY) > abs(endY - randPos.y)
            if (!probablyOnTheRoof) return randPos
        } while (++attempts < 5)

        return null
    }

    override fun getHireHandler(): BuildingHireHandler = hireHandler

    override fun getFurnacePos(): List<BlockPos>? =
        furnaceBlockPositions

    override fun findFurnaces() {
        this.furnaceBlockPositions = this.blockData?.referenceState
            ?.filter { it.blockState?.isOf(Blocks.FURNACE) == true }
            ?.map { it.pos.toImmutable() }
    }

    override fun getBars(): List<HudBar> {
        val hovered = ClientModUtils.getBuildingsManager().getHoveredBuilding().map { it.pos == pos }.orElse(false)

        val list = mutableListOf<HudBar>()
        if (this.health < 100 || hovered) {
            val color = when {
                this.health < 30 -> BarColor.RED
                this.health < 70 -> BarColor.YELLOW
                else -> BarColor.GREEN
            }
            list += HudBar(0, this.health / 100f, color)
        }


        val hireProgress = hireHandler
            .getProfessions()
            .map {
                hireHandler.getHireProgress(it.professionId)
            }
            .filter { it.queueLength > 0 }
            .minByOrNull { it.queueLength }
            ?.progress


        // TODO: add production queue
        val productionProgress: Int? = null



        if (hireProgress != null || productionProgress != null || hovered) {
            list += HudBar(1, (productionProgress ?: 0) / 100f, BarColor.BLUE)
            list += HudBar(2, (hireProgress ?: 0) / 100f, BarColor.PURPLE)
        }

        return list
    }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler {
        val propertyDelegate = object : PropertyDelegate {
            override fun get(index: Int): Int {
                return when(index) {
                    0 -> pos.x
                    1 -> pos.y
                    2 -> pos.z
                    3 -> selectedTabIndex
                    else -> error("Invalid property index")
                }
            }

            override fun set(index: Int, value: Int) {
                when (index) {
                    3 -> selectedTabIndex = value
                    else -> error("Invalid property index")
                }
            }

            override fun size(): Int = 4
        }

        return BuildingScreenHandler(syncId, propertyDelegate)
    }

    override fun getPos(): BlockPos = super<BlockEntity>.getPos()

    override fun getDisplayName(): Text {
        val nameStr = blueprintMetadata?.name ?: "Building"
        return Text.of(nameStr)
    }

    override fun destroy() {
        blockData?.actualState?.forEach {
            world?.removeBlock(it, false)
        }
        BlockPos.iterate(start, end)
            .map { it.toImmutable() }
            .filter { world?.getBlockState(it)?.fluidState?.isEmpty == false }
            .forEach { world?.setBlockState(it, Blocks.AIR.defaultState) }
        world?.removeBlock(pos, false)
        LOGGER.info("The building ${blueprintMetadata?.name ?: "No Name"} was destroyed!")
    }

    override fun getMetadata(): BlueprintMetadata {
        return this.blueprintMetadata ?: error("Blueprint metadata is not set")
    }

    override fun readNbt(nbt: NbtCompound) {
        fortressPos = BlockPos.fromLong(nbt.getLong("fortressCenter"))
        blueprintMetadata = BlueprintMetadata(nbt.getCompound("blueprintMetadata"))
        start = BlockPos.fromLong(nbt.getLong("start"))
        end = BlockPos.fromLong(nbt.getLong("end"))
        blockData = FortressBuildingBlockData.fromNbt(nbt.getCompound("blockData"))
        hireHandler.updateFromNbt(nbt.getCompound("hireHandler"))
    }

    override fun writeNbt(nbt: NbtCompound) {
        fortressPos?.let { nbt.putLong("fortressCenter", it.asLong()) }
        blueprintMetadata?.toNbt()?.let { nbt.put("blueprintMetadata", it) }
        start?.let { nbt.putLong("start", it.asLong()) }
        end?.let { nbt.putLong("end", it.asLong()) }
        blockData?.toNbt()?.let { nbt.put("blockData", it) }
        hireHandler.toNbt().let { nbt.put("hireHandler", it) }
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener> {
        val nbt = NbtCompound()
        writeNbt(nbt)
        return BlockEntityUpdateS2CPacket.create(this) { nbt }
    }

    override fun getHealth(): Int = blockData?.health ?: 100

    override fun getStart(): BlockPos? = start
    override fun getEnd(): BlockPos? = end

    override fun getFreeBed(world: World?): Optional<BlockPos> {
        val nullableBed = BlockPos
            .stream(start, end)
            .asSequence()
            .map { it.toImmutable() }
            .firstOrNull {
                val blockState = world?.getBlockState(it) ?: Blocks.AIR.defaultState
                blockState.isIn(BlockTags.BEDS) && !blockState.get(BedBlock.OCCUPIED)
            }
        return Optional.ofNullable(nullableBed)
    }


    override fun satisfiesRequirement(type: ProfessionType?, level: Int): Boolean =
        blueprintMetadata?.requirement?.satisfies(type, level) ?: false

    override fun attack(attacker: HostileEntity) {
        if (blockData?.attack(attacker) == true) attackers.add(attacker)
    }

    override fun getAttackers() = attackers
    override fun getRepairItemInfos(): List<ItemInfo> {
        return getRepairStates()
            .entries
            .groupingBy { it.value.block.asItem() }
            .eachCount()
            .map { ItemInfo(it.key, it.value) }
    }

    private fun getRepairStates() = blockData?.allBlockStatesToRepairTheBuilding ?: mapOf()

    override fun getBlocksToRepair(): Map<BlockPos, BlockState> = getRepairStates()

}