package net.remmintan.mods.minefortress.gui.building

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
import net.remmintan.mods.minefortress.core.interfaces.resources.IItemInfo
import net.remmintan.mods.minefortress.core.utils.CoreModUtils
import net.remmintan.mods.minefortress.core.utils.SimilarItemsHelper
import net.remmintan.mods.minefortress.gui.widget.ItemButtonWidget

private const val TEXT_COLOR = 0x404040

class BuildingScreen(handler: BuildingScreenHandler, playerInventory: PlayerInventory, title: Text) :
    HandledScreen<BuildingScreenHandler>(handler, playerInventory, title) {

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

    override fun handledScreenTick() {
        super.handledScreenTick()
        repairButton.active = handler.getHealth() < 100
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        super.mouseClicked(mouseX, mouseY, button)

        handler.tabs.forEach {
            if (it.isHovered(mouseX.toInt() - this.x, mouseY.toInt() - this.y)) {
                handler.selectedTab = it
                return true
            }
        }

        destroyButton.mouseClicked(mouseX, mouseY, button)
        repairButton.mouseClicked(mouseX, mouseY, button)

        return false
    }

    override fun drawBackground(context: DrawContext?, delta: Float, mouseX: Int, mouseY: Int) {
        context ?: return

        handler.tabs.forEach { tab -> if(tab != handler.selectedTab) renderTabIcon(context, tab) }
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)
        renderTabIcon(context, handler.selectedTab)
    }

    override fun drawForeground(context: DrawContext?, mouseX: Int, mouseY: Int) {
        context ?: return

        val selectedTab = handler.selectedTab
        context.drawText(this.textRenderer, selectedTab.name, x+5, y+5, TEXT_COLOR, false)

        when(selectedTab.type) {
            BuildingScreenTabType.INFO -> renderInfo(context, mouseX, mouseY)
            BuildingScreenTabType.WORKFORCE -> renderWorkforce(context, mouseX, mouseY)
            BuildingScreenTabType.PRODUCTION_LINE -> renderProductionLine(context, mouseX, mouseY)
        }

        handler.tabs.forEach {
            if(it.isHovered(mouseX-this.x, mouseY-this.y))
                context.drawTooltip(this.textRenderer, it.name, mouseX-x, mouseY-y)
        }

    }

    private fun  renderInfo(context: DrawContext, mouseX: Int, mouseY: Int) {
        val metadata = handler.getBlueprintMetadata()

        val texts = mutableListOf<Text>()

        val villagersCapacity: Int = metadata.capacity
        if (villagersCapacity > 0) {
            val villagersText = Text.literal("Capacity: ")
                .append("+$villagersCapacity")
                .formatted(Formatting.GRAY)
            texts.add(villagersText)
        }

        val requirement = metadata.requirement
        requirement.type?.let {
            val displayName = it.displayName
            val unlocksText = Text.literal("Unlocks: ").append(displayName).formatted(Formatting.GRAY)
            texts.add(unlocksText)

            val level = requirement.level
            val totalLevels = requirement.totalLevels
            val levelText = Text.literal("Level: ")
                .append((level + 1).toString())
                .append("/")
                .append(totalLevels.toString())
                .formatted(Formatting.GRAY)
            texts.add(levelText)
        }

        // rendering begins here
        var yDelta = 10

        // Name
        context.drawText(this.textRenderer, metadata.name, x+5, y+yDelta, TEXT_COLOR, false)
        yDelta += 5

        // Health
        val healthLabel = Text.of("Health: ")
        val labelWidth = this.textRenderer.getWidth(healthLabel)

        context.drawText(this.textRenderer, healthLabel, x+5, y+yDelta, TEXT_COLOR, false)
        val textureX = x+5 + labelWidth
        val textureY = y + yDelta

        val healthBasedWidth = MathHelper.map(handler.getHealth().toFloat(), 0f, 100f, 0f, 181f).toInt()
        context.drawTexture(BARS_TEXTURE, textureX, textureY, 0, 30, 181, 5)
        context.drawTexture(BARS_TEXTURE, textureX, textureY, 0, 35, healthBasedWidth, 5)

        yDelta += 10

        // Metadata texts
        texts.forEach {
            context.drawText(this.textRenderer, it, x+5, y+yDelta, TEXT_COLOR, false)
            yDelta += 10
        }

        // Manage buttons
        val manageLabel = Text.of("Manage: ")
        val manageLabelWidth = this.textRenderer.getWidth(manageLabel)

        context.drawText(this.textRenderer, manageLabel, x+5, y+yDelta, TEXT_COLOR, false)
        destroyButton.setPos(x+5+manageLabelWidth, y+yDelta-2)
        destroyButton.render(context, mouseX, mouseY, 0f)
        repairButton.setPos(x+5+manageLabelWidth+destroyButton.width+5, y+yDelta-2)
        repairButton.render(context, mouseX, mouseY, 0f)

        yDelta += 10

        // Upgrades
        context.drawText(this.textRenderer, Text.of("Building Upgrades:"), x+5, y+yDelta, TEXT_COLOR, false)
        yDelta += 5

        handler.upgrades.forEachIndexed { slotColumn, slot ->
            val blueprintId = slot.metadata.id
            val enoughResources = slot.isEnoughResources

            val slotRow = 0
            blueprintsRenderer.renderBlueprintInGui(
                context.matrices,
                blueprintId,
                BlockRotation.NONE,
                (x + 5).toFloat(),
                (y + yDelta).toFloat(),
                slotColumn,
                slotRow,
                enoughResources
            )

            val slotX = x + 5 + slotColumn * 18
            val slotY = y + yDelta
            if (isPointOverSlot(slotX, slotY, mouseX, mouseY)) {
                drawSlotHighlight(context, slotX, slotY, 10)

                if (fortressManager.isSurvival()) {
                    val stacks: List<IItemInfo> = slot.blockData.getStacks()
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

        if (handler.upgrades.isEmpty() && handler.getBlueprintMetadata().requirement.isMaxLevel()) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.of("Maximum building level reached"),
                x + backgroundWidth / 2,
                y + yDelta,
                TEXT_COLOR
            )
        }
    }

    private fun isPointOverSlot(slotX: Int, slotY: Int, mouseX: Int, mouseY: Int): Boolean {
        val screenX = this.x
        val screenY = this.y

        return mouseX >= screenX + slotX && mouseX < screenX + slotX + 18 && mouseY >= screenY + slotY && mouseY < screenY + slotY + 18
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
        val TABS_TEXTURE = Identifier("minefortress","textures/gui/tabs.png")
        val BACKGROUND_TEXTURE = Identifier("minefortress", "textures/gui/demo_background.png")
        val BARS_TEXTURE = Identifier("minefortress", "textures/gui/bars.png")
    }

}