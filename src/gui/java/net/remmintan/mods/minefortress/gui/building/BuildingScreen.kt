package net.remmintan.mods.minefortress.gui.building

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.BlockRotation
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.remmintan.mods.minefortress.core.dtos.ItemInfo
import net.remmintan.mods.minefortress.core.utils.CoreModUtils
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper
import net.remmintan.mods.minefortress.gui.widget.ItemButtonWidget

private const val PRIMARY_COLOR = 0x404040
private const val SECONDARY_COLOR = 0x707070
private const val HEADINGS_COLOR = 0x202020
private const val WHITE_COLOR = 0xFFFFFF

class BuildingScreen(handler: BuildingScreenHandler, playerInventory: PlayerInventory, title: Text) :
    HandledScreen<BuildingScreenHandler>(handler, playerInventory, title) {

    private val infoTab: InfoTab by lazy { InfoTab(handler, textRenderer) }

    init {
        this.backgroundWidth = 248
        this.backgroundHeight = 166
    }

    override fun init() {
        super.init()
        infoTab.x = x
        infoTab.y = y
        infoTab.backgroundWidth = backgroundWidth
        infoTab.backgroundHeight = backgroundHeight
        infoTab.init()
    }

    override fun resize(client: MinecraftClient?, width: Int, height: Int) {
        super.resize(client, width, height)
        infoTab.x = x
        infoTab.y = y
        infoTab.backgroundWidth = backgroundWidth
        infoTab.backgroundHeight = backgroundHeight
    }

    override fun handledScreenTick() {
        super.handledScreenTick()
        infoTab.tick()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        super.mouseClicked(mouseX, mouseY, button)

        if (handler.state == BuildingScreenHandler.State.TABS) {
            handler.tabs.forEach {
                if (it.isHovered(mouseX.toInt() - this.x, mouseY.toInt() - this.y)) {
                    handler.selectedTab = it
                }
            }
        }

        infoTab.onMouseClicked(mouseX, mouseY, button)

        return true
    }

    override fun drawBackground(context: DrawContext?, delta: Float, mouseX: Int, mouseY: Int) {
        context ?: return

        if (handler.state == BuildingScreenHandler.State.TABS)
            handler.tabs.forEach { tab -> if (tab != handler.selectedTab) renderTabIcon(context, tab) }
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)
        if (handler.state == BuildingScreenHandler.State.TABS)
            renderTabIcon(context, handler.selectedTab)
    }

    override fun drawForeground(context: DrawContext?, mouseX: Int, mouseY: Int) {
        context ?: return

        when (handler.state) {
            BuildingScreenHandler.State.TABS -> renderTabsContents(context, mouseX, mouseY)
            BuildingScreenHandler.State.DESTROY -> infoTab.renderDestroyConfirmation(context, mouseX, mouseY)
            BuildingScreenHandler.State.REPAIR -> infoTab.renderRepairConfirmation(context, mouseX, mouseY)
        }

    }

    private fun renderTabsContents(context: DrawContext, mouseX: Int, mouseY: Int) {
        val selectedTab = handler.selectedTab
        context.drawText(this.textRenderer, selectedTab.name, 7, 7, SECONDARY_COLOR, false)

        when (selectedTab.type) {
            BuildingScreenTabType.INFO -> infoTab.render(context, mouseX, mouseY)
            BuildingScreenTabType.WORKFORCE -> renderWorkforce(context, mouseX, mouseY)
            BuildingScreenTabType.PRODUCTION_LINE -> renderProductionLine(context, mouseX, mouseY)
        }

        handler.tabs.forEach {
            if (it.isHovered(mouseX - this.x, mouseY - this.y))
                context.drawTooltip(this.textRenderer, it.name, mouseX - x, mouseY - y)
        }
    }

    private fun renderWorkforce(context: DrawContext, mouseX: Int, mouseY: Int) {

    }

    private fun renderProductionLine(context: DrawContext, mouseX: Int, mouseY: Int) {

    }


    private fun renderTabIcon(context: DrawContext, tab: BuildingScreenTab) {
        val u = tab.tabU
        val v = if (tab === handler.selectedTab) 32 else 0
        val x: Int = this.x + tab.tabX
        val y = this.y - 28

        context.drawTexture(TABS_TEXTURE, x, y, u, v, 26, 32)

        context.matrices.push()
        context.matrices.translate(0.0f, 0.0f, 100.0f)

        val iconX = x + 5
        val iconY = y + 9
        context.drawItem(tab.icon, iconX, iconY)
        context.drawItemInSlot(textRenderer, tab.icon, iconX, iconY)

        context.matrices.pop()
    }


    companion object {
        val TABS_TEXTURE = Identifier("minefortress", "textures/gui/tabs.png")
        val BACKGROUND_TEXTURE = Identifier("minefortress", "textures/gui/demo_background.png")
        val BARS_TEXTURE = Identifier("minefortress", "textures/gui/bars.png")
    }

    interface ResizableTab {
        var x: Int
        var y: Int
        var backgroundWidth: Int
        var backgroundHeight: Int
    }

    class InfoTab(private val handler: BuildingScreenHandler, private val textRenderer: TextRenderer) : ResizableTab {

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


        fun init() {

        }

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

            yDelta = 80

            // Manage buttons
            val manageLabel = Text.of("Manage: ")
            val manageLabelWidth = this.textRenderer.getWidth(manageLabel)

            context.drawText(this.textRenderer, manageLabel, 0, yDelta, HEADINGS_COLOR, false)

            val translatedMouse = context.translateMousePosition(mouseX, mouseY)

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

            matrices.pop()

            matrices.push()
            matrices.scale(50f, 50f, 50f)
            handler.upgrades.forEachIndexed { slotColumn, slot ->
                val blueprintId = slot.metadata.id
                val enoughResources = slot.isEnoughResources

                val slotRow = 0
                blueprintsRenderer.renderBlueprintInGui(
                    matrices,
                    blueprintId,
                    BlockRotation.NONE,
                    slotColumn,
                    slotRow,
                    enoughResources
                )

                val slotX = x + 5 + slotColumn * 18
                val slotY = y + yDelta
                if (isPointOverSlot(slotX, slotY, mouseX, mouseY)) {
                    drawSlotHighlight(context, slotX, slotY, 10)

                    if (fortressManager.isSurvival()) {
                        val stacks: List<ItemInfo> = slot.blockData.getStacks()
                        for (i1 in stacks.indices) {
                            val stack = stacks[i1]
                            val hasItem = resourceManager.hasItem(stack, stacks)
                            val itemX = x + 25 + i1 % 10 * 30
                            val itemY = i1 / 10 * 20 + this.backgroundHeight
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

        private fun isPointOverSlot(slotX: Int, slotY: Int, mouseX: Int, mouseY: Int): Boolean {
            val screenX = this.x
            val screenY = this.y

            return mouseX >= screenX + slotX && mouseX < screenX + slotX + 18 && mouseY >= screenY + slotY && mouseY < screenY + slotY + 18
        }
    }

    class WorkforceTab {

        fun init() {

        }

        fun tick(mouseX: Int, mouseY: Int) {

        }

        fun render(context: DrawContext, mouseX: Int, mouseY: Int) {

        }

    }

}

fun DrawContext.translateMousePosition(x: Int, y: Int): Pair<Int, Int> {
    val positionMatrix = matrices.peek().positionMatrix
    val matX = positionMatrix.m30().toInt()
    val matY = positionMatrix.m31().toInt()

    return Pair(x - matX, y - matY)
}