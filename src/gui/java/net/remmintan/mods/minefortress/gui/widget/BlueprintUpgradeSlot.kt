package net.remmintan.mods.minefortress.gui.widget

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.remmintan.mods.minefortress.core.dtos.ItemInfo
import net.remmintan.mods.minefortress.core.dtos.blueprints.BlueprintSlot
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper
import net.remmintan.mods.minefortress.core.utils.isSurvivalFortress
import net.remmintan.mods.minefortress.gui.building.translateMousePosition

private const val SLOT_BACKGROUND_COLOR = (0xFF shl 24) or 0x8B8B8B
private const val SLOT_HIGHLIGHT_COLOR = (0x80 shl 24) or 0xFFFFFF
private const val UPGRADE_SLOT_SIDE_SIZE = 44

class BlueprintUpgradeSlot(
    val slot: BlueprintSlot,
    private val slotX: Int,
    private val slotY: Int,
    private val costsX: Int,
    private val costsY: Int,
    private val index: Int,
    private val textRenderer: TextRenderer,
    private val showItems: Boolean
) : Drawable {

    private val slotEndX = slotX + UPGRADE_SLOT_SIDE_SIZE
    private val slotEndY = slotY + UPGRADE_SLOT_SIDE_SIZE
    var hovered: Boolean = false
        private set

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.fillGradient(
            slotX, slotY, slotEndX, slotEndY, 10,
            SLOT_BACKGROUND_COLOR,
            SLOT_BACKGROUND_COLOR
        )

        val matrices = context.matrices
        if (isPointOverUpgradeSlot(matrices, slotX, slotY, mouseX, mouseY)) {
            DiffuseLighting.enableGuiDepthLighting()
            context.fillGradient(
                RenderLayer.getGuiOverlay(),
                slotX + 1,
                slotY + 1,
                slotEndX - 1,
                slotEndY - 1,
                SLOT_HIGHLIGHT_COLOR,
                SLOT_HIGHLIGHT_COLOR,
                110
            )

            val (newMx, newMy) = matrices.translateMousePosition(mouseX, mouseY)
            val metadata = slot.metadata
            val upgradeTooltipText = slot.getUpgradeTooltipText(metadata.requirement.level, metadata.capacity)
            context.drawTooltip(this.textRenderer, upgradeTooltipText, newMx, newMy)
            hovered = slot.isEnoughResources


            if (MinecraftClient.getInstance().isSurvivalFortress() && showItems) {
                val resourceManager = ClientModUtils.getFortressManager().resourceManager
                val stacks: List<ItemInfo> = slot.blockData.getStacks()
                val metRequirements = resourceManager.getMetRequirements(stacks)
                for (i1 in stacks.indices) {
                    val stack = stacks[i1]
                    val hasItem = metRequirements.getOrDefault(stack, false)
                    val itemX = costsX + i1 % 10 * 30
                    val itemY = costsY + i1 / 10 * 20
                    val convertedItem = SimilarItemsHelper.convertItemIconInTheGUI(stack.item())
                    context.drawItem(ItemStack(convertedItem), itemX, itemY)
                    context.drawText(
                        this.textRenderer,
                        stack.amount().toString(),
                        itemX + 18,
                        itemY + 4,
                        if (hasItem) 0xFFFFFF else 0xFF0000,
                        false
                    )
                }
            }
        } else {
            hovered = false
        }
    }

    fun renderUpgradeBlueprint() {
        val blueprintId = slot.metadata.id
        val enoughResources = slot.isEnoughResources

        val blueprintsRenderer = ClientModUtils.getRenderersProvider().get_GuiBlueprintsRenderer()
        blueprintsRenderer.renderBlueprintUpgrade(
            blueprintId,
            index,
            enoughResources
        )
    }

    private fun isPointOverUpgradeSlot(
        matrices: MatrixStack,
        slotX: Int,
        slotY: Int,
        mouseX: Int,
        mouseY: Int
    ): Boolean {
        val (mappedMouseX, mappedMouseY) = matrices.translateMousePosition(mouseX, mouseY)
        return mappedMouseX >= slotX && mappedMouseX < slotX + UPGRADE_SLOT_SIDE_SIZE && mappedMouseY >= slotY && mappedMouseY < slotY + UPGRADE_SLOT_SIDE_SIZE
    }

    companion object {
        fun create(
            x: Int,
            y: Int,
            index: Int,
            costsX: Int,
            costsY: Int,
            slot: BlueprintSlot,
            textRenderer: TextRenderer,
            showItems: Boolean = true
        ): BlueprintUpgradeSlot {
            val slotX = x + index * (UPGRADE_SLOT_SIDE_SIZE + 4)
            val slotY = y
            return BlueprintUpgradeSlot(slot, slotX, slotY, costsX, costsY, index, textRenderer, showItems)
        }
    }
}

