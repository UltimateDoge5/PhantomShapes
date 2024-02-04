package org.pkozak.screen

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import org.pkozak.Shape
import org.pkozak.shape.Cube
import java.awt.Color


class ShapesScreen(private val parent: Screen?, private val shapes: MutableList<Shape>) :
    Screen(Text.literal("Shapes manager")) {
    private var addShapeBtn: ButtonWidget? = null
    private var closeBtn: ButtonWidget? = null
    private var shapeListWidget: ShapeListWidget? = null

    override fun init() {
        shapeListWidget = this.addDrawableChild(ShapeListWidget(this, client!!, width, height, 40, 20, shapes))
        addShapeBtn = ButtonWidget.builder(Text.literal("New shape")) { addShape() }
            .dimensions(width / 2 - 205, 20, 200, 20)
            .tooltip(Tooltip.of(Text.literal("Create a new phantom shape"))).build()
        closeBtn = ButtonWidget.builder(Text.literal("Close")) { close() }
            .dimensions(width / 2 + 5, 20, 200, 20).build()

        addDrawableChild(addShapeBtn)
        addDrawableChild(closeBtn)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    private fun addShape() {
        val newShape = Cube(Color.YELLOW, Vec3d(-10.0, 0.0, 0.0), false, Vec3d(2.0, 5.0, 3.0))
        shapes.add(newShape)
        shapeListWidget!!.addEntry(ShapeListEntry(newShape, client!!))
    }
}