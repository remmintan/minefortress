package net.remmintan.mods.minefortress.gui.building

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.remmintan.mods.minefortress.core.dtos.ItemInfo
import net.remmintan.mods.minefortress.core.dtos.blueprints.BlueprintSlot
import net.remmintan.mods.minefortress.core.utils.CoreModUtils
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper
import net.remmintan.mods.minefortress.gui.building.BuildingScreen.Companion.HEADINGS_COLOR
import net.remmintan.mods.minefortress.gui.building.BuildingScreen.Companion.PRIMARY_COLOR
import net.remmintan.mods.minefortress.gui.building.BuildingScreen.Companion.SECONDARY_COLOR
import net.remmintan.mods.minefortress.gui.building.BuildingScreen.Companion.WHITE_COLOR
import net.remmintan.mods.minefortress.gui.widget.ItemButtonWidget

internal class InfoTab(private val handler: BuildingScreenHandler, private val textRenderer: TextRenderer) :
    ResizableTab {

    companion object {
        private val BARS_TEXTURE = Identifier("minefortress", "textures/gui/bars.png")
        private const val SLOT_BACKGROUND_COLOR = (0xFF shl 24) or 0x8B8B8B
        private const val SLOT_HIGHLIGHT_COLOR = (0x80 shl 24) or 0xFFFFFF
        private const val UPGRADE_SLOT_SIDE_SIZE = 44
    }

    override var x: Int = 0
    override var y: Int = 0
    override var backgroundWidth: Int = 0
    override var backgroundHeight: Int = 0

    private val blueprintsRenderer = CoreModUtils.getRenderersProvider().get_GuiBlueprintsRenderer()
    private val fortressManager = CoreModUtils.getFortressClientManager()
    private val resourceManager = fortressManager.resourceManager

    private val destroyButton: ItemButtonWidget = ItemButtonWidget(
        0,
        0,
        Items.TNT,
        { _: ButtonWidget -> handler.destroy() },
        "Destroy this building"
    )

    private val repairButton: ItemButtonWidget = ItemButtonWidget(
        0,
        0,
        Items.IRON_INGOT,
        { _: ButtonWidget -> handler.repair() },
        "Repair this building"
    )

    private val destroyConfirmationButton = ButtonWidget
        .builder(Text.of("Destroy")) { handler.destroy() }
        .dimensions(0, 0, 102, 20)
        .build()

    private val repairConfirmationButton = ButtonWidget
        .builder(Text.of("Repair")) { handler.repair() }
        .dimensions(0, 0, 102, 20)
        .build()

    private val cancelButton = ButtonWidget
        .builder(Text.of("Cancel")) { handler.cancel() }
        .dimensions(0, 0, 102, 20)
        .build()

    private var hoveredUpgrade: BlueprintSlot? = null

    fun tick() {
        repairButton.active = handler.getHealth() < 100
        repairConfirmationButton.active = handler.hasSelectedPawns()
    }

    fun render(context: DrawContext, mouseX: Int, mouseY: Int) {
        val metadata = handler.getBlueprintMetadata()

        val texts = mutableListOf<Pair<Text, Text>>()

        val villagersCapacity: Int = metadata.capacity
        if (villagersCapacity > 0) {
            val capacityLabel = Text.literal("Capacity: ").formatted(Formatting.DARK_GRAY)
            val capacityText = Text.literal("+$villagersCapacity").formatted(Formatting.DARK_PURPLE)

            texts.add(capacityLabel to capacityText)
        }

        val requirement = metadata.requirement
        requirement.type?.let {
            val unlocksLabel = Text.literal("Unlocks: ").formatted(Formatting.DARK_GRAY)
            val unlocksText = Text.literal(it.displayName).formatted(Formatting.DARK_PURPLE)
            texts.add(unlocksLabel to unlocksText)

            val levelLabel = Text.literal("Level: ").formatted(Formatting.DARK_GRAY)
            val levelText = Text.literal((requirement.level + 1).toString())
                .append("/")
                .append(requirement.totalLevels.toString())
                .formatted(Formatting.DARK_PURPLE)

            texts.add(levelLabel to levelText)
        }

        // rendering begins here
        val matrices = context.matrices
        matrices.push()
        matrices.translate(7f, 17f, 0f)
        var yDelta = 0

        // Name
        context.drawText(this.textRenderer, metadata.name, 0, yDelta, HEADINGS_COLOR, false)
        yDelta += 13

        // Health
        val healthLabel = Text.of("Health: ")
        val healthLabelWidth = this.textRenderer.getWidth(healthLabel)

        context.drawText(this.textRenderer, healthLabel, 0, yDelta, PRIMARY_COLOR, false)
        val textureX = healthLabelWidth
        val textureY = yDelta + 1

        val healthBasedWidth = MathHelper.map(handler.getHealth().toFloat(), 0f, 100f, 0f, 182f).toInt()
        context.drawTexture(BARS_TEXTURE, textureX, textureY, 0, 30, 182, 5)
        context.drawTexture(BARS_TEXTURE, textureX, textureY, 0, 35, healthBasedWidth, 5)

        yDelta += 13

        // Metadata texts
        texts.forEach {
            val label = it.first
            val text = it.second
            context.drawText(this.textRenderer, label, 0, yDelta, PRIMARY_COLOR, false)
            val labelWidth = this.textRenderer.getWidth(label)
            context.drawText(this.textRenderer, text, labelWidth, yDelta, SECONDARY_COLOR, false)
            yDelta += 12
        }

        yDelta = 75

        // Manage buttons
        val manageLabel = Text.of("Manage: ")
        val manageLabelWidth = this.textRenderer.getWidth(manageLabel)

        context.drawText(this.textRenderer, manageLabel, 0, yDelta, HEADINGS_COLOR, false)

        val translatedMouse = matrices.translateMousePosition(mouseX, mouseY)

        destroyButton.setPos(manageLabelWidth, yDelta - 10)
        destroyButton.render(context, translatedMouse.first, translatedMouse.second, 0f)
        repairButton.setPos(manageLabelWidth + destroyButton.width + 5, yDelta - 10)
        repairButton.render(context, translatedMouse.first, translatedMouse.second, 0f)

        yDelta += 17

        // Upgrades
        context.drawText(this.textRenderer, Text.of("Building Upgrades:"), 0, yDelta, HEADINGS_COLOR, false)
        yDelta += 25

        if (handler.upgrades.isEmpty() || handler.getBlueprintMetadata().requirement.isMaxLevel()) {
            val infoLabel = "Maximum building level reached"
            context.drawText(
                this.textRenderer,
                infoLabel,
                backgroundWidth / 2 - this.textRenderer.getWidth(infoLabel) / 2,
                yDelta,
                0xA67C00,
                false
            )
        }

        hoveredUpgrade = null
        handler.upgrades.forEachIndexed { index, slot ->
            val slotX = 1 + index * (UPGRADE_SLOT_SIDE_SIZE + 4)
            val slotY = yDelta - 16
            val slotEndX = slotX + UPGRADE_SLOT_SIDE_SIZE
            val slotEndY = slotY + UPGRADE_SLOT_SIDE_SIZE
            context.fillGradient(slotX, slotY, slotEndX, slotEndY, 10, SLOT_BACKGROUND_COLOR, SLOT_BACKGROUND_COLOR)

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
                val upgradeTooltipText = slot.getUpgradeTooltipText(metadata.requirement.level, metadata.capacity)
                context.drawTooltip(this.textRenderer, upgradeTooltipText, newMx, newMy)
                if (slot.isEnoughResources) hoveredUpgrade = slot

                if (fortressManager.isSurvival()) {
                    val stacks: List<ItemInfo> = slot.blockData.getStacks()
                    for (i1 in stacks.indices) {
                        val stack = stacks[i1]
                        val hasItem = resourceManager.hasItem(stack, stacks)
                        val itemX = 25 + i1 % 10 * 30
                        val itemY = i1 / 10 * 20
                        val convertedItem = SimilarItemsHelper.convertItemIconInTheGUI(stack.item())
                        context.drawItem(ItemStack(convertedItem), itemX, itemY)
                        context.drawText(
                            this.textRenderer,
                            stack.amount().toString(),
                            itemX + 17,
                            itemY + 7,
                            if (hasItem) 0xFFFFFF else 0xFF0000,
                            false
                        )
                    }
                }
            }
        }

        matrices.pop()


        val matrixStack = RenderSystem.getModelViewStack()
        matrixStack.push()
        matrixStack.translate(x.toDouble() + 13, y.toDouble() + yDelta + 10, 0.0)
        RenderSystem.applyModelViewMatrix()
        handler.upgrades.forEachIndexed { index, slot ->
            val blueprintId = slot.metadata.id
            val enoughResources = slot.isEnoughResources

            blueprintsRenderer.renderBlueprintUpgrade(
                blueprintId,
                index,
                enoughResources
            )
        }
        matrixStack.pop()
        RenderSystem.applyModelViewMatrix()


    }

    fun renderDestroyConfirmation(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.matrices.loadIdentity()
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.of("Destroy this building?"),
            x + backgroundWidth / 2,
            y + backgroundHeight / 2 - 40,
            WHITE_COLOR
        )

        destroyConfirmationButton.setPosition(x + this.backgroundWidth / 2 - 102 - 3, y + backgroundHeight / 2 + 5)
        cancelButton.setPosition(x + this.backgroundWidth / 2 + 3, y + backgroundHeight / 2 + 5)

        destroyConfirmationButton.render(context, mouseX, mouseY, 0f)
        cancelButton.render(context, mouseX, mouseY, 0f)
    }

    fun renderRepairConfirmation(context: DrawContext, mouseX: Int, mouseY: Int) {
        context.matrices.loadIdentity()
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.of("Repair building?"),
            x + backgroundWidth / 2,
            y + 25,
            WHITE_COLOR
        )
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.of("Required items:"),
            x + backgroundWidth / 2,
            y + 40,
            WHITE_COLOR
        )
        val enoughItems = handler.getEnoughItems()
        var itemX = x + 10
        var itemY = y + 50
        handler.getItemsToRepair().forEach {
            context.drawItem(it.item.defaultStack, itemX, itemY)
            val text = "x${it.amount}"
            val color = if (enoughItems.getValue(it)) 0xFFFFFF else 0xb81d13
            context.drawText(textRenderer, text, itemX + 16 + 2, itemY + 6, color, false)

            val columnWidth = 16 + textRenderer.getWidth(text)
            itemX += columnWidth + 5
            if (itemX + columnWidth > x + backgroundWidth - 10) {
                itemX = x + 10
                itemY += 20
            }
        }

        repairConfirmationButton.setPosition(x + this.backgroundWidth / 2 - 102 - 3, itemY + 45)
        cancelButton.setPosition(x + this.backgroundWidth / 2 + 3, itemY + 45)

        repairConfirmationButton.render(context, mouseX, mouseY, 0f)
        cancelButton.render(context, mouseX, mouseY, 0f)

        if (!handler.hasSelectedPawns()) {
            val warnLabel = Text.of("Select pawns who will repair the building!")
            context.drawText(
                this.textRenderer,
                warnLabel,
                x + backgroundWidth / 2 - this.textRenderer.getWidth(warnLabel) / 2,
                itemY + 30,
                0xb81d13,
                false
            )
        }
    }

    fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        when (handler.state) {
            BuildingScreenHandler.State.TABS -> {
                destroyButton.mouseClicked(mouseX, mouseY, button)
                repairButton.mouseClicked(mouseX, mouseY, button)
                hoveredUpgrade?.let { handler.upgrade(it) }
            }

            BuildingScreenHandler.State.DESTROY -> {
                destroyConfirmationButton.mouseClicked(mouseX, mouseY, button)
                cancelButton.mouseClicked(mouseX, mouseY, button)
            }

            BuildingScreenHandler.State.REPAIR -> {
                repairConfirmationButton.mouseClicked(mouseX, mouseY, button)
                cancelButton.mouseClicked(mouseX, mouseY, button)
            }
        }

        return true
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
}