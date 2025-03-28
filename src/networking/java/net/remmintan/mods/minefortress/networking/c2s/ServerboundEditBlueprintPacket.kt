package net.remmintan.mods.minefortress.networking.c2s

import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.Vec3i
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintDataLayer
import net.remmintan.mods.minefortress.core.interfaces.blueprints.BlueprintGroup
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IStructureBlockData
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType
import net.remmintan.mods.minefortress.core.interfaces.networking.FortressC2SPacket
import net.remmintan.mods.minefortress.core.interfaces.server.IFortressServer
import net.remmintan.mods.minefortress.core.utils.getManagersProvider

class ServerboundEditBlueprintPacket : FortressC2SPacket {
    private val blueprintId: String?
    private val blueprintName: String?
    private val actionType: ActionType
    private val floorLevel: Int
    private val blueprintGroup: BlueprintGroup

    private constructor(
        blueprintId: String?,
        blueprintName: String?,
        floorLevel: Int,
        actionType: ActionType,
        blueprintGroup: BlueprintGroup
    ) {
        this.blueprintId = blueprintId
        this.blueprintName = blueprintName
        this.floorLevel = floorLevel
        this.actionType = actionType
        this.blueprintGroup = blueprintGroup
    }

    constructor(buf: PacketByteBuf) {
        this.blueprintId = buf.readNullable { obj: PacketByteBuf -> obj.readString() }
        this.blueprintName = buf.readNullable { obj: PacketByteBuf -> obj.readString() }
        this.floorLevel = buf.readInt()
        this.blueprintGroup = buf.readEnumConstant(BlueprintGroup::class.java)
        this.actionType = buf.readEnumConstant(
            ActionType::class.java
        )
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeNullable(this.blueprintId) { b, id -> b.writeString(id) }
        buf.writeNullable(this.blueprintName) { b, name -> b.writeString(name) }
        buf.writeInt(this.floorLevel)
        buf.writeEnumConstant(blueprintGroup)
        buf.writeEnumConstant(actionType)
    }

    override fun handle(server: MinecraftServer, player: ServerPlayerEntity) {
        val managersProvider = getManagersProvider(player)
        if (player.server is IFortressServer) {
            val srv = player.server as IFortressServer
            val mp = player.getManagersProvider()
            if (actionType == ActionType.REMOVE) {
                mp.get_BlueprintManager().remove(blueprintId)
            } else {
                val blueprintsWorld = srv._BlueprintWorld
                if (actionType == ActionType.EDIT) {
                    val blockData: IStructureBlockData = mp.get_BlueprintManager()
                        .blockDataManager
                        .getBlockData(blueprintId, BlockRotation.NONE)
                    val blueprintData = blockData
                        .getLayer(BlueprintDataLayer.GENERAL)

                    val metadata = mp.get_BlueprintManager().get(blueprintId)

                    blueprintsWorld.setBlueprintMetadata(
                        player,
                        blueprintId,
                        blueprintName,
                        blueprintGroup,
                        metadata.capacity,
                        metadata.requirement.type ?: ProfessionType.NONE
                    )
                    blueprintsWorld.putBlueprintInAWorld(blueprintData, player, blockData.size, floorLevel)
                } else if (actionType == ActionType.CREATE) {
                    blueprintsWorld.setBlueprintMetadata(
                        player,
                        blueprintId,
                        blueprintName,
                        blueprintGroup,
                        4,
                        ProfessionType.NONE
                    )
                    blueprintsWorld.putBlueprintInAWorld(HashMap(), player, Vec3i(1, 1, 1), floorLevel)
                }
                player.moveToWorld(blueprintsWorld.world)
            }
        }
    }

    private enum class ActionType {
        EDIT, CREATE, REMOVE
    }

    companion object {
        @JvmStatic
        fun edit(id: String?, floorLevel: Int, group: BlueprintGroup): ServerboundEditBlueprintPacket {
            return ServerboundEditBlueprintPacket(id, null, floorLevel, ActionType.EDIT, group)
        }

        @JvmStatic
        fun add(name: String?, group: BlueprintGroup): ServerboundEditBlueprintPacket {
            return ServerboundEditBlueprintPacket(null, name, 0, ActionType.CREATE, group)
        }

        @JvmStatic
        fun remove(id: String?): ServerboundEditBlueprintPacket {
            return ServerboundEditBlueprintPacket(id, null, 0, ActionType.REMOVE, BlueprintGroup.LIVING_HOUSES)
        }
    }
}
