package org.pkozak.ui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.math.ColorHelper
import java.awt.Color

class ColorSquare(
    private var x: Int, private var y: Int, private val color: Color
) : ClickableWidget(x, y, 16, 16, Text.of("")) {
    constructor(color: Color) : this(0, 0, color)

    override fun getX(): Int {
        return x
    }

    override fun getY(): Int {
        return y
    }

    override fun setX(x: Int) {
        this.x = x
    }

    override fun setY(y: Int) {
        this.y = y
    }

    override fun getWidth(): Int {
        return 16
    }

    override fun getHeight(): Int {
        return 16
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        context.drawBorder(x, y, 16, 16, ColorHelper.Argb.getArgb(140, 255, 255, 255))
        context.fill(x + 1, y + 1, x + 15, y + 15, color.rgb)
    }

    // We don't need to handle any mouse events
    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {}
}


