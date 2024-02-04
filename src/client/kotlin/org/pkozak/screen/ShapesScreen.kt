package org.pkozak.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import org.pkozak.Shape
import org.pkozak.shape.Cube
import java.awt.Color


class ShapesScreen(private val parent: Screen?, internal val shapes: MutableList<Shape>) :
    Screen(Text.literal("Shapes manager")) {
    private var addShapeBtn: ButtonWidget? = null
    private var closeBtn: ButtonWidget? = null
    private var shapeListWidget: ShapeListWidget? = null

    override fun init() {
        shapeListWidget = ShapeListWidget(this, client!!, width, height, 60, 24, shapes)
        addShapeBtn = ButtonWidget.builder(Text.literal("New shape")) { client?.setScreen(NewShapeScreen(this, null)) }
            .dimensions(width / 2 - 205, height - 40, 200, 20)
            .tooltip(Tooltip.of(Text.literal("Create a new phantom shape"))).build()
        closeBtn = ButtonWidget.builder(Text.literal("Close")) { close() }
            .dimensions(width / 2 + 5, height - 40, 200, 20).build()

        addDrawableChild(addShapeBtn)
        addDrawableChild(closeBtn)
        addDrawableChild(shapeListWidget)
    }


    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        shapeListWidget!!.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, Color.WHITE.rgb)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    fun addShape(newShape: Shape) {
        shapes.add(newShape)
        shapeListWidget!!.addEntry(ShapeListEntry(shapeListWidget!!, newShape, client!!))
    }
}