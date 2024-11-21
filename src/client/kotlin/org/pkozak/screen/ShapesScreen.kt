package org.pkozak.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.*
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.math.ColorHelper
import org.joml.Vector4i
import org.pkozak.PhantomShapesClient
import org.pkozak.shape.Shape
import org.pkozak.ui.ColorSquare
import org.pkozak.ui.IconButton
import org.pkozak.ui.Icons
import org.pkozak.util.SavedDataManager
import java.awt.Color

class ShapesScreen(private val parent: Screen?, internal val shapes: MutableList<Shape>) :
    Screen(Text.literal("Shapes manager")) {
    private var addShapeBtn: ButtonWidget? = null
    private var closeBtn: ButtonWidget? = null
    private var grid: GridWidget = GridWidget().setColumnSpacing(10).setRowSpacing(8)
    private var adder = grid.createAdder(8)
    private var rowsBgCords = listOf<Vector4i>()

    override fun init() {
        initGrid()

        addShapeBtn =
            ButtonWidget.builder(Text.literal("New shape")) { client?.setScreen(ShapeEditorScreen(this, null)) }
                .dimensions(width / 2 - 205, height - 40, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Create a new phantom shape"))).build()
        closeBtn =
            ButtonWidget.builder(ScreenTexts.DONE) { close() }.dimensions(width / 2 + 5, height - 40, 200, 20).build()

        val settingsBtn = ButtonWidget.Builder(Text.literal("Settings")) {
            client?.setScreen(SettingsScreen.generateScreen(this))
        }
            .tooltip(Tooltip.of(Text.of("Settings of the mod")))
            .dimensions(width - 85, 5, 80, 20)
            .build()

        addDrawableChild(addShapeBtn)
        addDrawableChild(closeBtn)
        addDrawableChild(settingsBtn)
    }

    // We need to reset the grid if we want to refresh it since we can't remove rows from it
    private fun initGrid() {
        // First, remove all children from the previous grid
        grid.forEachChild {
            remove(it)
        }

        rowsBgCords = listOf()

        // Then, create a new grid
        grid = GridWidget().setColumnSpacing(10).setRowSpacing(6)

        // Add the header row
        adder = grid.createAdder(8)
        adder.add(TextWidget(Text.of(""), this.textRenderer)) // We need an empty space for the visibility toggle
        adder.add(TextWidget(Text.of("Name"), this.textRenderer))
        adder.add(TextWidget(Text.of("Type"), this.textRenderer))
        adder.add(TextWidget(Text.of("Color"), this.textRenderer))
        adder.add(TextWidget(Text.of("Position"), this.textRenderer))
        adder.add(TextWidget(Text.of("Blocks"), this.textRenderer))
        adder.add(TextWidget(Text.of("Actions"), this.textRenderer), 2)

        // Add all shapes to the grid
        shapes.forEach { addShapeRow(it) }
        grid.refreshPositions()
        SimplePositioningWidget.setPos(this.grid, 0, 40, this.width, this.height, 0.5f, 0f)

        var i = 0
        var x1 = 0
        var y1 = 0
        var x2 = 0
        var y2 = 0
        grid.forEachChild { child ->
            // Skip the header row from the background drawing
            if (i < 7) {
                i++
                addDrawableChild(child)
                return@forEachChild
            }
            if (i == 7) {
                x1 = child.x
            }
            if (child.y > y1) {
                y1 = child.y
            }
            if (child.width + child.x > x2) {
                x2 = child.x + child.width
            }
            if (child.height + child.y > y2) {
                y2 = child.y + child.height
            }

            // For every new row
            if (i % 7 == 0 && i > 7) {
                // Add the coordinates of the previous row to the list for background drawing
                // Change values by +/- 2 to add some padding
                rowsBgCords += Vector4i(x1 - 2, y1 - 2, x2 + 2, y2)
                // x1 and x2 is the same for every row, so we don't need to reset it
                y1 = 0
                y2 = 0
            }
            addDrawableChild(child)
            i++
        }
    }

    private fun addShapeRow(shape: Shape) {
        val icon = if (shape.enabled) Icons.VISIBLE_ICON else Icons.INVISIBLE_ICON
        val toggleButton = IconButton.Builder {
            shape.toggleVisibility()
            it.icon = if (shape.enabled) Icons.VISIBLE_ICON else Icons.INVISIBLE_ICON
        }.size(24, 20).tooltip(Tooltip.of(Text.of("Toggle visibility"))).icon(icon).build()
        adder.add(toggleButton)

        val nameWidget = TextWidget(Text.of(shape.name), this.textRenderer)
        nameWidget.height = 22 // We need to set the height manually, otherwise the text will be not centered vertically
        adder.add(nameWidget)

        adder.add(IconWidget.create(18, 18, shape.getIcon()))
        adder.add(ColorSquare(shape.color))

        val posWidget = TextWidget(Text.of("${shape.pos.x}, ${shape.pos.y}, ${shape.pos.z}"), this.textRenderer)
        posWidget.height = 22
        adder.add(posWidget)

        val blockAmount = if (shape.blockAmount == -1) "Unknown" else shape.blockAmount.toString()
        val blockAmountWidget = TextWidget(Text.of(blockAmount), this.textRenderer)
        blockAmountWidget.height = 22
        adder.add(blockAmountWidget)

        adder.add(ButtonWidget.builder(Text.literal("Edit")) {
            client?.setScreen(ShapeEditorScreen(this, shape))
        }.size(50, 20).build())

        adder.add(ButtonWidget.builder(Text.literal("Delete").withColor(Colors.LIGHT_RED)) {
            removeShape(shape)
        }.size(50, 20).build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, Color.WHITE.rgb)

        for (i in rowsBgCords.indices) {
            if (i % 2 == 0) {
                context.fill(
                    rowsBgCords[i].x,
                    rowsBgCords[i].y,
                    rowsBgCords[i].z,
                    rowsBgCords[i].w,
                    ColorHelper.getArgb(32, 0, 0, 0)
                )
            } else {
                context.fill(
                    rowsBgCords[i].x,
                    rowsBgCords[i].y,
                    rowsBgCords[i].z,
                    rowsBgCords[i].w,
                    ColorHelper.getArgb(64, 0, 0, 0)
                )
            }
        }

        if (PhantomShapesClient.overwriteProtection) {
            context.drawCenteredTextWithShadow(
                textRenderer,
                Text.of("There were problems while loading shapes file. Any changes won't be saved!"),
                width / 2,
                height - 60,
                0xff5555
            )
        }
    }

    override fun close() {
        if (!PhantomShapesClient.overwriteProtection) {
            SavedDataManager.saveShapes(shapes)
        }
        client?.setScreen(parent)
    }

    fun addShape(newShape: Shape) {
        shapes.add(newShape)
        // We need to reinitialize the grid from scratch
        initGrid()
    }

    private fun removeShape(shape: Shape) {
        shapes.remove(shape)
        // We need to reinitialize the grid from scratch
        initGrid()
    }
}