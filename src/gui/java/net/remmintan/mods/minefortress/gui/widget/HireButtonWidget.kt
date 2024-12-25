package net.remmintan.mods.minefortress.gui.widget

import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

class HireButtonWidget private constructor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Text,
    onPress: PressAction,
    narrationSupplier: NarrationSupplier
) :
    ButtonWidget(x, y, width, height, message, onPress, narrationSupplier) {

    companion object {
        fun builder(text: Text, onPress: PressAction): Builder {
            return Builder(text, onPress)
        }
    }

    override fun clicked(mouseX: Double, mouseY: Double): Boolean {
        return this.active && this.visible && this.hovered
    }

    class Builder(private val message: Text, private val onPress: PressAction) {
        private var tooltip: Tooltip? = null
        private var x = 0
        private var y = 0
        private var width = 150
        private var height = 20
        private var narrationSupplier: NarrationSupplier = DEFAULT_NARRATION_SUPPLIER

        fun position(x: Int, y: Int): Builder {
            this.x = x
            this.y = y
            return this
        }

        fun width(width: Int): Builder {
            this.width = width
            return this
        }

        fun size(width: Int, height: Int): Builder {
            this.width = width
            this.height = height
            return this
        }

        fun dimensions(x: Int, y: Int, width: Int, height: Int): Builder {
            return position(x, y).size(width, height)
        }

        fun tooltip(tooltip: Tooltip?): Builder {
            this.tooltip = tooltip
            return this
        }

        fun narrationSupplier(narrationSupplier: NarrationSupplier): Builder {
            this.narrationSupplier = narrationSupplier
            return this
        }

        fun build(): HireButtonWidget {
            val buttonWidget = HireButtonWidget(
                this.x,
                this.y,
                this.width,
                this.height,
                this.message,
                this.onPress,
                this.narrationSupplier
            )
            buttonWidget.tooltip = tooltip
            return buttonWidget
        }
    }

}