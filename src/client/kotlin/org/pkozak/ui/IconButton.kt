package org.pkozak.ui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class IconButton(
    x: Int, y: Int, width: Int, height: Int, var icon: Identifier?, onPress: PressAction,
    narrationSupplier: NarrationSupplier?
) : ButtonWidget(
    x, y, width, height, Text.empty(),
    onPress, narrationSupplier
) {
    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        if (icon != null) {
            val x = x + width / 2 - 8
            val y = y + height / 2 - 8
            context?.drawGuiTexture(icon, x, y, 16, 16)
        }
    }

    companion object {
        fun builder(onPress: PressAction): Builder {
            return Builder(onPress)
        }

        class Builder(private val onPress: PressAction) {
            private var tooltip: Tooltip? = null
            private var x = 0
            private var y = 0
            private var width = 150
            private var height = 20
            private var narrationSupplier: NarrationSupplier = DEFAULT_NARRATION_SUPPLIER
            private var icon: Identifier? = null

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

            fun icon(icon: Identifier): Builder {
                this.icon = icon
                return this
            }

            fun dimensions(x: Int, y: Int, width: Int, height: Int): Builder {
                return position(x, y).size(width, height)
            }

            fun tooltip(tooltip: Tooltip?): Builder {
                this.tooltip = tooltip
                return this
            }

            fun build(): IconButton {
                val buttonWidget = IconButton(
                    this.x,
                    this.y,
                    this.width,
                    this.height,
                    this.icon,
                    this.onPress,
                    this.narrationSupplier
                )
                buttonWidget.tooltip = tooltip
                return buttonWidget
            }
        }
    }
}