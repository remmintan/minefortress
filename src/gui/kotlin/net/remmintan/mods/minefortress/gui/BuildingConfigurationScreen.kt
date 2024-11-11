package net.remmintan.mods.minefortress.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.CyclingButtonWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.remmintan.mods.minefortress.core.interfaces.blueprints.ProfessionType

class BuildingConfigurationScreen(
    handler: BuildingConfigurationScreenHandler,
    playerInventory: PlayerInventory,
    title: Text
) : HandledScreen<BuildingConfigurationScreenHandler>(handler, playerInventory, title) {


    override fun init() {
        super.init()
        val grid = GridWidget()
        grid.setPosition(this.x - 100, this.y + 30)
        grid.mainPositioner.margin(2).alignLeft()


        val adder = grid.createAdder(2)

        val professionsLabel = TextWidget(Text.of("Hires:"), this.textRenderer)
        val professionSelector = CyclingButtonWidget
            .builder { it: ProfessionType -> Text.of(it.displayName) }
            .values(ProfessionType.entries)
            .initially(handler.getProfession())
            .build(this.x + 2, this.y + 2, 200, 20, Text.of("Profession"))
            { btn, prof ->
                handler.setProfession(prof)
            }
        adder.add(professionsLabel, grid.copyPositioner().marginTop(9))
        adder.add(professionSelector)

        val capacityLabel = TextWidget(Text.of("Building capacity:"), this.textRenderer)
        val capacityWidget = TextFieldWidget(this.textRenderer, 200, 20, Text.of(handler.getCapacity().toString()))
        capacityWidget.setChangedListener { text ->
            val capacity = text.toIntOrNull()
            if (capacity != null && capacity < 101) {
                handler.setCapacity(capacity)
            }
        }
        capacityWidget.setTextPredicate { text -> text.isEmpty() || text.toIntOrNull() != null && text.toInt() < 101 }
        adder.add(capacityLabel, grid.copyPositioner().marginTop(9))
        adder.add(capacityWidget)


        grid.refreshPositions()
        grid.setPosition(this.width / 2 - grid.width / 2, this.y + 30)
        grid.forEachChild(this::addDrawableChild)
    }

    override fun drawForeground(context: DrawContext?, mouseX: Int, mouseY: Int) {
        context?.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            (this.backgroundWidth) / 2,
            this.titleY,
            0xFFFFFF
        )
    }

    override fun drawBackground(context: DrawContext?, delta: Float, mouseX: Int, mouseY: Int) {
    }
}