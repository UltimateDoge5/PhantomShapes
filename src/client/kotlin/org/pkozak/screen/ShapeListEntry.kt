package org.pkozak.screen

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.math.ColorHelper
import org.pkozak.PhantomShapesClient.INVISIBLE_ICON
import org.pkozak.PhantomShapesClient.VISIBLE_ICON
import org.pkozak.PhantomShapesClient.logger
import org.pkozak.Shape

class ShapeListEntry(
    private val widget: ShapeListWidget,
    internal val shape: Shape,
    private val client: MinecraftClient
) :
    ElementListWidget.Entry<ShapeListEntry>() {
    private var deleteBtn: ButtonWidget? = null
    private var editBtn: ButtonWidget? = null
    private var toggleBtn: ButtonWidget? = null

    init {
        deleteBtn = ButtonWidget.builder(Text.literal("Delete").withColor(Colors.LIGHT_RED)) { delete() }
            .dimensions(0, 0, 50, 32).build()

        editBtn = ButtonWidget.builder(Text.literal("Edit")) { client.setScreen(NewShapeScreen(widget.parent, shape)) }
            .dimensions(0, 0, 50, 32).build()

        // TODO: This is not working I hate this
        toggleBtn = ButtonWidget.builder(Text.empty()) {
            logger.info("Toggled visibility of shape ${shape.name} to ${shape.enabled}")
            shape.toggleVisibility()
        }.dimensions(0, 0, 24, 24).build()
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
        // Mojang does that in widgets, so I'll do it too
        val l = y + (entryHeight - client.textRenderer.fontHeight) / 2
        val i = x + 4
        val j = y + (entryHeight - 24) / 2
        val k = i + 24 + 4

        // First render the shape name
        context.drawText(
            this.client.textRenderer,
            shape.name,
            24,
            l,
            0xFFFFFF,
            false
        )

        // Then the shape icon
        context.drawGuiTexture(
            shape.getIcon(),
            124,
            y - 1,
            20,
            20
        )

        // Render shape color
        context.drawBorder(i, j, 16, 16, ColorHelper.Argb.getArgb(140, 255, 255, 255))
        context.fill(i + 1, j + 1, i + 15, j + 15, shape.color.rgb)

        // Render buttons
        deleteBtn!!.y = y + 1
        deleteBtn!!.x = x + entryWidth - 25
        deleteBtn!!.height = entryHeight - 2
        deleteBtn!!.render(context, mouseX, mouseY, tickDelta)

        editBtn!!.y = y + 1
        editBtn!!.x = x + entryWidth - 75 - 4
        editBtn!!.height = entryHeight - 2
        editBtn!!.render(context, mouseX, mouseY, tickDelta)

        toggleBtn!!.y = y + 1
        toggleBtn!!.x = 24
        toggleBtn!!.height = entryHeight - 2
        toggleBtn!!.render(context, mouseX, mouseY, tickDelta)
    }

    private fun getIconTexture(): Identifier {
        return if (shape.enabled) VISIBLE_ICON else INVISIBLE_ICON
    }

    private fun delete() {
        widget.removeEntry(this)
    }

    override fun children(): MutableList<out Element> {
        return mutableListOf(deleteBtn!!, editBtn!!)
    }

    override fun selectableChildren(): MutableList<out Selectable> {
        return mutableListOf(deleteBtn!!, editBtn!!)
    }
}