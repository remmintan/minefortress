package net.remmintan.mods.minefortress.blocks.building

import net.minecraft.block.BedBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.enums.BedPart
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.tag.BlockTags
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.remmintan.mods.minefortress.blocks.FortressBlocks
import net.remmintan.mods.minefortress.core.dtos.buildings.BlueprintMetadata
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationArea
import net.remmintan.mods.minefortress.core.interfaces.automation.area.IAutomationBlockInfo
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.buildings.IFortressBuilding
import net.remmintan.mods.minefortress.gui.BuildingScreenHandler
import java.time.LocalDateTime
import java.util.*

private const val MAX_BLOCKS_PER_UPDATE = 10

class FortressBuildingBlockEntity(pos: BlockPos?, state: BlockState?) :
    BlockEntity(FortressBlocks.BUILDING_ENT_TYPE, pos, state),
    NamedScreenHandlerFactory,
    IFortressBuilding {

    private var blueprintMetadata: BlueprintMetadata? = null
    private var start: BlockPos? = null
    private var end: BlockPos? = null
    private var blockData: FortressBuildingBlockData? = null

    private var automationArea: IAutomationArea? = null

    private val attackers: MutableSet<HostileEntity> = HashSet()
    private var beds = listOf<BlockPos>()

    fun init(
        metadata: BlueprintMetadata,
        start: BlockPos,
        end: BlockPos,
        blockData: Map<BlockPos, BlockState>,
    ) {
        this.blueprintMetadata = metadata
        this.start = start
        this.end = end

        val movedBlocksData = blockData.mapKeys { it.key.add(start) }

        this.blockData = FortressBuildingBlockData(movedBlocksData, metadata.floorLevel)
        this.automationArea = BuildingAutomationAreaProvider(start, end, metadata.requirement)
    }

    fun tick(world: World?) {
        world ?: return
        blockData?.checkTheNextBlocksState(MAX_BLOCKS_PER_UPDATE, world as? ServerWorld)

        beds = BlockPos.iterate(start, end)
            .filter {
                val blockState = world.getBlockState(it)
                blockState.isIn(BlockTags.BEDS) && blockState.get(BedBlock.PART) == BedPart.HEAD
            }
            .map { it.toImmutable() }
            .toList()

        this.markDirty()
        if (this.world?.isClient == false) {
            val state = this.cachedState
            this.world?.updateListeners(this.pos, state, state, Block.NOTIFY_ALL)
        }
    }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler {
        return BuildingScreenHandler(syncId)
    }

    override fun getPos(): BlockPos = super<BlockEntity>.getPos()

    override fun getDisplayName(): Text {
        val nameStr = blueprintMetadata?.name ?: "Building"
        return Text.of(nameStr)
    }

    override fun destroy() {
        TODO("Not yet implemented")
    }

    override fun getName(): String = blueprintMetadata?.name ?: "Building"

    override fun readNbt(nbt: NbtCompound) {
        blueprintMetadata = BlueprintMetadata(nbt.getCompound("blueprintMetadata"))
        start = BlockPos.fromLong(nbt.getLong("start"))
        end = BlockPos.fromLong(nbt.getLong("end"))
        blockData = FortressBuildingBlockData.fromNbt(nbt.getCompound("blockData"))
        automationArea = BuildingAutomationAreaProvider(start!!, end!!, blueprintMetadata!!.requirement)
    }

    override fun writeNbt(nbt: NbtCompound) {
        blueprintMetadata?.toNbt()?.let { nbt.put("blueprintMetadata", it) }
        start?.let { nbt.putLong("start", it.asLong()) }
        end?.let { nbt.putLong("end", it.asLong()) }
        blockData?.toNbt()?.let { nbt.put("blockData", it) }
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener> {
        val nbt = NbtCompound()
        writeNbt(nbt)
        return BlockEntityUpdateS2CPacket.create(this) { nbt }
    }

    override fun getId(): UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

    override fun getHealth(): Int = blockData?.health ?: 100

    override fun getStart(): BlockPos? = start
    override fun getEnd(): BlockPos? = end

    override fun getFreeBed(world: World?): Optional<BlockPos> =
        beds.firstOrNull {
            val blockState = world?.getBlockState(it) ?: Blocks.AIR.defaultState
            blockState.isIn(BlockTags.BEDS) && blockState.get(BedBlock.OCCUPIED)
        }.let { Optional.ofNullable(it) }

    override fun getBedsCount(): Int = beds.size

    override fun satisfiesRequirement(type: ProfessionType?, level: Int): Boolean =
        blueprintMetadata?.requirement?.satisfies(type, level) ?: false

    override fun attack(attacker: HostileEntity) {
        if (blockData?.attack(attacker) == true) attackers.add(attacker)
    }

    override fun getAttackers() = attackers
    override fun getAllBlockStatesToRepairTheBuilding() = blockData?.allBlockStatesToRepairTheBuilding ?: mapOf()

    // IAutomationArea
    override fun getUpdated(): LocalDateTime = automationArea?.updated ?: error("Automation area provider is not set")

    override fun update() {
        automationArea?.update()
    }

    override fun iterator(world: World?): MutableIterator<IAutomationBlockInfo> =
        automationArea?.iterator(world) ?: error("Automation area provider is not set")


}