package org.pkozak.screen

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.math.ColorHelper
import org.pkozak.Shape

class ShapeListEntry(
    private val parent: ShapeListWidget,
    internal val shape: Shape,
    private val client: MinecraftClient
) :
    ElementListWidget.Entry<ShapeListEntry>() {
    private val LIGHT_GRAY_COLOR: Int = ColorHelper.Argb.getArgb(140, 255, 255, 255)
    private var deleteBtn: ButtonWidget? = null
    private var editBtn: ButtonWidget? = null
    private var hovered = false

    init {
        deleteBtn = ButtonWidget.builder(Text.literal("Delete").withColor(Colors.LIGHT_RED)) { delete() }
            .dimensions(0, 0, 50, 32).build()

        editBtn = ButtonWidget.builder(Text.literal("Edit")) { }
            .dimensions(0, 0, 50, 32).build()
    }

    override fun render(
        context: DrawContext,
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
        this.hovered = hovered

        // Render shape name
        context.drawText(
            this.client.textRenderer,
            shape.name,
            k,
            l,
            LIGHT_GRAY_COLOR,
            false
        )

        // Render shape color
        context.drawBorder(i, j, 16, 16, LIGHT_GRAY_COLOR)
        context.fill(i + 1, j + 1, i + 15, j + 15, shape.color.rgb)

        // Render the buttons
        if (hovered) {
            deleteBtn!!.y = y
            deleteBtn!!.x = x + entryWidth - 25
            deleteBtn!!.height = entryHeight
            deleteBtn!!.render(context, mouseX, mouseY, tickDelta)

            editBtn!!.y = y
            editBtn!!.x = x + entryWidth - 75
            editBtn!!.height = entryHeight
            editBtn!!.render(context, mouseX, mouseY, tickDelta)

        }
    }

    private fun delete() {
        parent.removeEntry(this)
    }

    override fun children(): MutableList<out Element> {
        if (!hovered) return mutableListOf()
        return mutableListOf(deleteBtn!!, editBtn!!)
    }

    override fun selectableChildren(): MutableList<out Selectable> {
        if (!hovered) return mutableListOf()
        return mutableListOf(deleteBtn!!, editBtn!!)
    }
}