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
import net.minecraft.util.Identifier
import net.minecraft.util.math.ColorHelper
import org.pkozak.shape.Shape
import org.pkozak.ui.IconButton
import org.pkozak.ui.Icons

class ShapeListEntry(
    private val widget: ShapeListWidget, internal val shape: Shape, private val client: MinecraftClient
) : ElementListWidget.Entry<ShapeListEntry>() {
    private var deleteBtn: ButtonWidget? = null
    private var editBtn: ButtonWidget? = null
    private var toggleBtn: IconButton? = null

    init {
        editBtn =
            ButtonWidget.builder(Text.literal("Edit")) { client.setScreen(ShapeEditorScreen(widget.parent, shape)) }
                .dimensions(360, 0, 50, 32).build()

        deleteBtn = ButtonWidget.builder(Text.literal("Delete").withColor(Colors.LIGHT_RED)) { delete() }
            .dimensions(425, 0, 50, 32).build()

        toggleBtn = IconButton.builder {
            shape.toggleVisibility()
            toggleBtn!!.icon = getToggleTexture()
        }
            .dimensions(4, 0, 24, 20)
            .icon(getToggleTexture())
            .tooltip(Tooltip.of(Text.of("Toggle visibility")))
            .build()
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

        if (index % 2 == 0) {
            context.fill(x, y, x + entryWidth, y + entryHeight, ColorHelper.Argb.getArgb(32, 0, 0, 0))
        } else {
            context.fill(x, y, x + entryWidth, y + entryHeight, ColorHelper.Argb.getArgb(64, 0, 0, 0))
        }

        // First render the shape name
        context.drawText(
            this.client.textRenderer, shape.name, x + 40, l, 0xFFFFFF, false
        )

        // Then the shape icon
        context.drawGuiTexture(
            shape.getIcon(), x + 150, y + 2, 16, 16
        )

        // Render shape color
        context.drawBorder(x + 190, y + 2, 16, 16, ColorHelper.Argb.getArgb(140, 255, 255, 255))
        context.fill(x + 191, y + 3, x + 205, y + 17, shape.color.rgb)

        // Render shape position
        context.drawText(
            this.client.textRenderer,
            Text.literal("${shape.pos.x}, ${shape.pos.y}, ${shape.pos.z}"),
            x + 230,
            l,
            0xFFFFFF,
            false
        )

        // Render block amount
        context.drawText(
            this.client.textRenderer,
            Text.literal("${if (shape.blockAmount > -1) shape.blockAmount else "Unknown"}"),
            x + 354,
            l,
            0xFFFFFF,
            false
        )

        // Render buttons
        toggleBtn!!.y = y + 2
        toggleBtn!!.x = x + 4
        toggleBtn!!.height = entryHeight - 4
        toggleBtn!!.render(context, mouseX, mouseY, tickDelta)

        editBtn!!.y = y + 2
        editBtn!!.x = x + 408
        editBtn!!.height = entryHeight - 4
        editBtn!!.render(context, mouseX, mouseY, tickDelta)

        deleteBtn!!.y = y + 2
        deleteBtn!!.x = x + 462
        deleteBtn!!.height = entryHeight - 4
        deleteBtn!!.render(context, mouseX, mouseY, tickDelta)
    }

    private fun getToggleTexture(): Identifier {
        return if (shape.enabled) Icons.VISIBLE_ICON else Icons.INVISIBLE_ICON
    }

    private fun delete() {
        widget.removeEntry(this)
    }

    override fun children(): MutableList<out Element> {
        return mutableListOf(deleteBtn!!, editBtn!!, toggleBtn!!)
    }

    override fun selectableChildren(): MutableList<out Selectable> {
        return mutableListOf(deleteBtn!!, editBtn!!, toggleBtn!!)
    }
}