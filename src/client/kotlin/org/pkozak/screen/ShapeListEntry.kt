package org.pkozak.screen

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.util.math.ColorHelper
import org.pkozak.Shape

class ShapeListEntry(private val shape: Shape, private val client: MinecraftClient) :
    ElementListWidget.Entry<ShapeListEntry>() {
    val LIGHT_GRAY_COLOR: Int = ColorHelper.Argb.getArgb(140, 255, 255, 255)
    override fun render(
        context: DrawContext?,
        index: Int,
        y: Int,
        x: Int,
        entryWidth: Int,
        entryHeight: Int,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        tickDelta: Float
    ) {
        val l = y + (entryHeight - client.textRenderer.fontHeight) / 2
        val i = x + 4
        val j = y + (entryHeight - 24) / 2
        val k = i + 24 + 4

        // Render shape name
        context?.drawText(
            this.client.textRenderer,
            shape.name,
            k,
            l + 12,
            LIGHT_GRAY_COLOR,
            false
        )
    }

    override fun children(): MutableList<out Element> {
        TODO("Not yet implemented")
    }

    override fun selectableChildren(): MutableList<out Selectable> {
        TODO("Not yet implemented")
    }
}