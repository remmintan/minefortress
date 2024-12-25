package net.remmintan.mods.minefortress.gui.building

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.remmintan.mods.minefortress.gui.building.handlers.InfoTabState

class BuildingScreen(handler: BuildingScreenHandler, playerInventory: PlayerInventory, title: Text) :
    HandledScreen<BuildingScreenHandler>(handler, playerInventory, title) {

    companion object {
        private val TABS_TEXTURE = Identifier("minefortress", "textures/gui/tabs.png")
        private val BACKGROUND_TEXTURE = Identifier("minefortress", "textures/gui/demo_background.png")

        const val PRIMARY_COLOR = 0x404040
        const val SECONDARY_COLOR = 0x707070
        const val HEADINGS_COLOR = 0x202020
        const val WHITE_COLOR = 0xFFFFFF
    }

    private val infoTab by lazy { InfoTab(handler, textRenderer) }
    private val workforceTab by lazy { WorkforceTab(handler, textRenderer) }

    init {
        this.backgroundWidth = 248
        this.backgroundHeight = 166
    }

    override fun init() {
        super.init()
        resizeAllTabs()
    }

    override fun resize(client: MinecraftClient?, width: Int, height: Int) {
        super.resize(client, width, height)
        resizeAllTabs()
    }

    override fun handledScreenTick() {
        super.handledScreenTick()
        infoTab.tick()
        workforceTab.tick()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        super.mouseClicked(mouseX, mouseY, button)

        if (handler.getInfoTabState() == InfoTabState.TABS) {
            handler.tabs.forEach {
                if (it.isHovered(mouseX.toInt() - this.x, mouseY.toInt() - this.y)) {
                    handler.selectedTab = it
                }
            }
        }

        infoTab.onMouseClicked(mouseX, mouseY, button)
        workforceTab.onMouseClicked(mouseX, mouseY, button)

        return true
    }

    override fun drawBackground(context: DrawContext?, delta: Float, mouseX: Int, mouseY: Int) {
        context ?: return

        if (handler.getInfoTabState() == InfoTabState.TABS)
            handler.tabs.forEach { tab -> if (tab != handler.selectedTab) renderTabIcon(context, tab) }
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)
        if (handler.getInfoTabState() == InfoTabState.TABS)
            renderTabIcon(context, handler.selectedTab)
    }

    override fun drawForeground(context: DrawContext?, mouseX: Int, mouseY: Int) {
        context ?: return

        when (handler.getInfoTabState()) {
            InfoTabState.TABS -> renderTabsContents(context, mouseX, mouseY)
            InfoTabState.DESTROY -> infoTab.renderDestroyConfirmation(context, mouseX, mouseY)
            InfoTabState.REPAIR -> infoTab.renderRepairConfirmation(context, mouseX, mouseY)
        }

    }

    private fun resizeAllTabs() {
        for (tab in listOf(infoTab, workforceTab)) {
            tab.x = x
            tab.y = y
            tab.backgroundWidth = backgroundWidth
            tab.backgroundHeight = backgroundHeight
        }
    }

    private fun renderTabsContents(context: DrawContext, mouseX: Int, mouseY: Int) {
        val selectedTab = handler.selectedTab
        context.drawText(this.textRenderer, selectedTab.name, 7, 7, SECONDARY_COLOR, false)

        when (selectedTab.type) {
            BuildingScreenTabType.INFO -> infoTab.render(context, mouseX, mouseY)
            BuildingScreenTabType.WORKFORCE -> workforceTab.render(context, mouseX, mouseY)
            BuildingScreenTabType.PRODUCTION_LINE -> renderProductionLine(context, mouseX, mouseY)
        }

        handler.tabs.forEach {
            if (it.isHovered(mouseX - this.x, mouseY - this.y))
                context.drawTooltip(this.textRenderer, it.name, mouseX - x, mouseY - y)
        }
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

}

fun MatrixStack.translateMousePosition(x: Int, y: Int): Pair<Int, Int> {
    val positionMatrix = this.peek().positionMatrix
    val matX = positionMatrix.m30().toInt()
    val matY = positionMatrix.m31().toInt()

    return Pair(x - matX, y - matY)
}