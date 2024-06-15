package org.pkozak.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.IconWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.Colors
import org.pkozak.PhantomShapesClient
import org.pkozak.PhantomShapesClient.logger
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

    override fun init() {
        initGrid()

        addShapeBtn =
            ButtonWidget.builder(Text.literal("New shape")) { client?.setScreen(ShapeEditorScreen(this, null)) }
                .dimensions(width / 2 - 205, height - 40, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Create a new phantom shape"))).build()
        closeBtn =
            ButtonWidget.builder(ScreenTexts.DONE) { close() }.dimensions(width / 2 + 5, height - 40, 200, 20).build()

        addDrawableChild(addShapeBtn)
        addDrawableChild(closeBtn)
    }

    // We need to reset the grid if we want to refresh it since we can't remove rows from it
    private fun initGrid() {
        // First, remove all children from the previous grid
        grid.forEachChild {
            remove(it)
        }

        // Then, create a new grid
        grid = GridWidget().setColumnSpacing(10).setRowSpacing(8)
        grid.setPosition(8, 40)

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
        grid.forEachChild { child ->
            addDrawableChild(child)
        }
    }

    private fun addShapeRow(shape: Shape) {
        val icon = if (shape.enabled) Icons.VISIBLE_ICON else Icons.INVISIBLE_ICON
        val toggleButton = IconButton.Builder {
            shape.toggleVisibility()
            it.icon = if (shape.enabled) Icons.VISIBLE_ICON else Icons.INVISIBLE_ICON
        }.size(24, 20).tooltip(Tooltip.of(Text.of("Toggle visibility"))).icon(icon).build()
        adder.add(toggleButton)

        adder.add(TextWidget(Text.of(shape.name), this.textRenderer))
        adder.add(IconWidget.create(16, 16, shape.getIcon()))
//        adder.add(TextWidget(Text.of(shape.color.rgb.toString()), this.textRenderer))
        adder.add(ColorSquare(shape.color))
        adder.add(TextWidget(Text.of("${shape.pos.x}, ${shape.pos.y}, ${shape.pos.z}"), this.textRenderer))

        val blockAmount = if (shape.blockAmount == -1) "Unknown" else shape.blockAmount.toString()
        adder.add(TextWidget(Text.of(blockAmount), this.textRenderer))

        adder.add(ButtonWidget.builder(Text.literal("Edit")) {
            client?.setScreen(ShapeEditorScreen(this, shape))
        }.width(50).build())

        adder.add(ButtonWidget.builder(Text.literal("Delete").withColor(Colors.LIGHT_RED)) {
            removeShape(shape)
        }.width(50).build())
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, Color.WHITE.rgb)

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
        logger.info("Before: ${shapes.size}")
        shapes.remove(shape)
        // We need to reinitialize the grid from scratch
        logger.info("afterr: ${shapes.size}")
        initGrid()
    }
}