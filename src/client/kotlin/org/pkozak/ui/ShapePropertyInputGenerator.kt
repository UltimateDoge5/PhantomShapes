package org.pkozak.ui

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.GridWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.Widget
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.pkozak.shape.*
import java.util.function.Consumer

/**
 * This widget is responsible for generating the input fields for the shape properties.
 * It generates inputs for unique properties of each shape type
 * `Name`, `position` and `color` are common for all shapes.
 *
 * Rotation is technically not unique, but not shapes can practically benefit from it, so sometimes its omitted.
 * @param textRenderer Minecraft text renderer, retrieved from the context or the client
 */
class ShapePropertyInputGenerator(private val textRenderer: TextRenderer) {
    var shapeType: ShapeType = ShapeType.CUBE
    private var editedShape: Shape? = null

    // Store all the elements in a list so we can return them for the required overload
    private val elements = mutableListOf<ShapePropertyInput>()

    var grid: GridWidget = GridWidget().setColumnSpacing(10).setRowSpacing(6)

    private val adder = grid.createAdder(3)

    fun generateInputs(): GridWidget {
        grid = GridWidget().setColumnSpacing(10).setRowSpacing(6) // Reset the grid
        elements.clear()

        when (shapeType) {
            ShapeType.CUBE -> {
                elements.add(ShapePropertyInput(textRenderer, "Width", "3"))
                elements.add(ShapePropertyInput(textRenderer, "Height", "3"))
                elements.add(ShapePropertyInput(textRenderer, "Depth", "3"))
            }

            ShapeType.CYLINDER -> {
                elements.add(ShapePropertyInput(textRenderer, "Radius", "5"))
                elements.add(ShapePropertyInput(textRenderer, "Height", "3"))
                addRotationInputs()
            }

            ShapeType.SPHERE -> {
                elements.add(ShapePropertyInput(textRenderer, "Radius", "5"))
            }

            ShapeType.TUNNEL -> {
                // TODO: Migrate tunnel
            }

            ShapeType.ARCH -> {
                elements.add(ShapePropertyInput(textRenderer, "Radius", "6"))
                elements.add(ShapePropertyInput(textRenderer, "Height", "5"))
                addRotationInputs()
            }

            ShapeType.POLYGON -> {
                elements.add(ShapePropertyInput(textRenderer, "Radius", "5"))
                elements.add(ShapePropertyInput(textRenderer, "Height", "1"))
                elements.add(ShapePropertyInput(textRenderer, "Sides", "6", ShapePropertyInput.Constraint(3, 12)))
                addRotationInputs()
            }
        }

        elements.forEach { adder.add(it) }
        return grid
    }

    private fun addRotationInputs() {
        val constraint = ShapePropertyInput.Constraint(0, 270)
        elements.add(ShapePropertyInput(textRenderer, "Rotation X", "0", constraint))
        elements.add(ShapePropertyInput(textRenderer, "Rotation Y", "0", constraint))
        elements.add(ShapePropertyInput(textRenderer, "Rotation Z", "0", constraint))
    }

    fun checkForError(): String? {
        elements.forEach {
            val error = it.checkForError()
            if (error != null) {
                return error
            }

        }
        return null
    }

    fun editShape(editedShape: Shape): GridWidget {
        this.shapeType = editedShape.type
        val grid = generateInputs()

        // Load in the current values, we know what the order is of the inputs based on the generate function
        when (shapeType) {
            ShapeType.CUBE -> {
                val listener = fun(_: String) {
                    if (elements.slice(0..2).any { it.checkForError() !== null }) return
                    val vec = Vec3d(
                        elements[0].getValue().toDouble(),
                        elements[1].getValue().toDouble(),
                        elements[2].getValue().toDouble()
                    )
                    editedShape.pos = vec
                    editedShape.shouldRerender = true
                }

                elements[0].setValue((editedShape as Cube).dimensions.x.toString(), listener)
                elements[1].setValue(editedShape.dimensions.y.toString(), listener)
                elements[2].setValue(editedShape.dimensions.z.toString(), listener)
            }

            ShapeType.SPHERE -> {
                elements[0].setValue((editedShape as Sphere).radius.toString(), fun(radius: String) {
                    if (elements[0].checkForError() !== null) return
                    editedShape.radius = radius.toInt()
                })
            }

            ShapeType.CYLINDER -> {
                elements[0].setValue((editedShape as Cylinder).radius.toString(), fun(radius: String) {
                    if (elements[0].checkForError() !== null) return
                    editedShape.radius = radius.toInt()
                })

                elements[1].setValue(editedShape.height.toString(), fun(height: String) {
                    if (elements[1].checkForError() !== null) return
                    editedShape.height = height.toInt()
                })
            }

            ShapeType.TUNNEL -> TODO()
            ShapeType.ARCH -> {
                elements[0].setValue((editedShape as Arch).radius.toString(), fun(radius: String) {
                    if (elements[0].checkForError() !== null) return
                    editedShape.radius = radius.toInt()
                })

                elements[1].setValue(editedShape.width.toString(), fun(width: String) {
                    if (elements[1].checkForError() !== null) return
                    editedShape.width = width.toInt()
                })
            }

            ShapeType.POLYGON -> {
                elements[0].setValue((editedShape as Polygon).radius.toString(), fun(radius: String) {
                    if (elements[0].checkForError() !== null) return
                    editedShape.radius = radius.toInt()
                })

                elements[1].setValue(editedShape.height.toString(), fun(height: String) {
                    if (elements[1].checkForError() !== null) return
                    editedShape.height = height.toInt()
                })

                elements[2].setValue(editedShape.sides.toString(), fun(sides: String) {
                    if (elements[2].checkForError() !== null) return
                    editedShape.sides = sides.toInt()
                })
            }
        }

        return grid
    }

    /**
     * Get the shape properties from the input fields
     */
    fun <T : Shape> getShapeProperties(shape: T): T {
        when (shapeType) {
            ShapeType.CUBE -> {
                val width = elements[0].getValue().toInt()
                val height = elements[1].getValue().toInt()
                val depth = elements[2].getValue().toInt()
                (shape as Cube).dimensions = Vec3i(width, height, depth)
                return shape
            }

            ShapeType.SPHERE -> {
                val radius = elements[0].getValue().toInt()
                (shape as Sphere).radius = radius
                return shape
            }

            ShapeType.CYLINDER -> {
                val radius = elements[0].getValue().toInt()
                val height = elements[1].getValue().toInt()
                (shape as Cylinder).radius = radius
                shape.height = height
                return shape
            }

            ShapeType.TUNNEL -> TODO()
            ShapeType.ARCH -> {
                val radius = elements[0].getValue().toInt()
                val width = elements[1].getValue().toInt()
                (shape as Arch).radius = radius
                shape.width = width
                return shape
            }

            ShapeType.POLYGON -> {
                val radius = elements[0].getValue().toInt()
                val height = elements[1].getValue().toInt()
                val sides = elements[2].getValue().toInt()
                (shape as Polygon).radius = radius
                shape.height = height
                shape.sides = sides
                return shape
            }
        }
    }

    private class ShapePropertyInput(private val textRenderer: TextRenderer, private val label: String, value: String) :
        Widget, Drawable {
        private var x = 0
        private var y = 0

        constructor(textRenderer: TextRenderer, label: String, value: String, constraints: Constraint) : this(
            textRenderer,
            label,
            value
        ) {
            this.constraints = constraints
        }

        private val input = TextFieldWidget(textRenderer, 50, 20, Text.of(value))
        private var constraints = Constraint(1, null)

        fun checkForError(): String? {
            if (input.text.isEmpty()) {
                return "Shape $label cannot be empty."
            }
            val value = input.text.toIntOrNull() ?: return "Shape $label must be a number."

            if (constraints.min > value) {
                return "Shape $label must be greater than or equal ${constraints.min}."
            }

            if (constraints.max != null && constraints.max!! < value) {
                return "Shape $label must be less than or equal ${constraints.max}."
            }
            return null
        }

        fun getValue(): String {
            return input.text
        }

        fun setValue(value: String, listener: Consumer<String>) {
            input.text = value
            input.setChangedListener(listener)
        }

        override fun setX(x: Int) {
            this.x = x
            input.x = x
        }

        override fun setY(y: Int) {
            this.y = y
            input.y = y + 10
        }

        override fun getX(): Int {
            return x
        }

        override fun getY(): Int {
            return y
        }

        override fun getWidth(): Int {
            return input.width
        }

        override fun getHeight(): Int {
            return input.height
        }

        override fun forEachChild(consumer: Consumer<ClickableWidget>) {
            return listOf(input).forEach { consumer.accept(it) }
        }

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            context.drawText(textRenderer, label, x, y, 0xffffff, false)
            input.render(context, mouseX, mouseY, delta)
        }

        /**
         * @param min The minimum value for the input (inclusive)
         * @param max The maximum value for the input (inclusive). If null, there is no maximum.
         */
        data class Constraint(val min: Int, val max: Int?)
    }
}
