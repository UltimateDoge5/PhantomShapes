package org.pkozak.ui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.function.Supplier

class IconButton(
    x: Int, y: Int, width: Int, height: Int, var icon: Identifier?, private var iconPressAction: (IconButton) -> Unit
) : PressableWidget(x, y, width, height, Text.of("")) {
    override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        if (icon != null) {
            val x = x + width / 2 - 8
            val y = y + height / 2 - 8
            context?.drawGuiTexture(RenderLayer::getGuiTextured, icon, x, y, 16, 16)
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        appendDefaultNarrations(builder)
    }

    override fun onPress() {
        iconPressAction(this)
    }

    class Builder(private val onPress: (IconButton) -> Unit) {
        private var tooltip: Tooltip? = null
        private var x = 0
        private var y = 0
        private var width = 150
        private var height = 20
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
            val iconButton = IconButton(
                this.x,
                this.y,
                this.width,
                this.height,
                this.icon,
                this.onPress
            )
            iconButton.tooltip = tooltip
            return iconButton
        }
    }
}