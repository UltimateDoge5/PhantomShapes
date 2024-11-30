package org.pkozak.ui

import net.minecraft.client.font.TextRenderer
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

    // Store all the elements in a list so we can return them for the required overload
    private val elements = mutableListOf<ShapePropertyInput>()

    var grid: GridWidget = GridWidget().setColumnSpacing(4).setRowSpacing(20)
    private var adder = grid.createAdder(3)

    fun generateInputs(): GridWidget {
        grid = GridWidget().setColumnSpacing(4).setRowSpacing(20) // Reset the grid
        adder = grid.createAdder(3) // Reset the adder

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

            ShapeType.ARCH -> {
                elements.add(ShapePropertyInput(textRenderer, "Radius", "6"))
                elements.add(ShapePropertyInput(textRenderer, "Height", "5"))
                addRotationInputs()
            }

            ShapeType.POLYGON -> {
                elements.add(ShapePropertyInput(textRenderer, "Radius", "5", ShapePropertyInput.Constraint(3, null)))
                elements.add(ShapePropertyInput(textRenderer, "Height", "1"))
                elements.add(ShapePropertyInput(textRenderer, "Sides", "6", ShapePropertyInput.Constraint(3, 12)))
                addRotationInputs()
            }
        }

        // If there are rotation inputs, make sure they are in the next row, by making the last element of the first row take up more columns
        // We have 2 possible edge cases
        when (elements.size) {
            4 -> {
                adder.add(elements[0], 3)
                adder.add(elements[1])
                adder.add(elements[2])
                adder.add(elements[3])
            }

            5 -> {
                adder.add(elements[0])
                adder.add(elements[1], 2)
                adder.add(elements[2])
                adder.add(elements[3])
                adder.add(elements[4])
            }

            else -> {
                for (element in elements) {
                    adder.add(element)
                }
            }
        }

        return grid
    }

    fun getLabels(): List<Label> {
        val labels = mutableListOf<Label>()
        for (element in elements) {
            labels.add(element.getLabel())
        }
        return labels
    }

    private fun addRotationInputs() {
        val constraint = ShapePropertyInput.Constraint(0, 270)
        elements.add(ShapePropertyInput(textRenderer, "Rotation X", "0", constraint))
        elements.add(ShapePropertyInput(textRenderer, "Rotation Y", "0", constraint))
        elements.add(ShapePropertyInput(textRenderer, "Rotation Z", "0", constraint))
    }

    private fun addRotationListeners(editedShape: Shape) {
        val inputs = elements.slice(elements.size - 3 until elements.size)
        inputs[0].setValue(editedShape.rotation.x.toString(), fun(rotationX: String) {
            editedShape.rotation.x = rotationX.toInt()
            editedShape.shouldRerender = true
        })
        inputs[1].setValue(editedShape.rotation.y.toString(), fun(rotationY: String) {
            editedShape.rotation.y = rotationY.toInt()
            editedShape.shouldRerender = true
        })
        inputs[2].setValue(editedShape.rotation.z.toString(), fun(rotationZ: String) {
            editedShape.rotation.z = rotationZ.toInt()
            editedShape.shouldRerender = true
        })
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
                    // We still need to check for errors, because the listener groups all the inputs together
                    if (elements.slice(0..2).any { it.checkForError() !== null }) return
                    val vec = Vec3i(
                        elements[0].getValue().toInt(),
                        elements[1].getValue().toInt(),
                        elements[2].getValue().toInt()
                    )
                    (editedShape as Cube).dimensions = vec
                    editedShape.shouldRerender = true
                }

                elements[0].setValue((editedShape as Cube).dimensions.x.toString(), listener)
                elements[1].setValue(editedShape.dimensions.y.toString(), listener)
                elements[2].setValue(editedShape.dimensions.z.toString(), listener)
            }

            ShapeType.SPHERE -> {
                elements[0].setValue((editedShape as Sphere).radius.toString(), fun(radius: String) {
                    editedShape.radius = radius.toInt()
                    editedShape.shouldRerender = true
                })
            }

            ShapeType.CYLINDER -> {
                elements[0].setValue((editedShape as Cylinder).radius.toString(), fun(radius: String) {
                    editedShape.radius = radius.toInt()
                    editedShape.shouldRerender = true
                })

                elements[1].setValue(editedShape.height.toString(), fun(height: String) {
                    editedShape.height = height.toInt()
                    editedShape.shouldRerender = true
                })
                addRotationListeners(editedShape)
            }

            ShapeType.ARCH -> {
                elements[0].setValue((editedShape as Arch).radius.toString(), fun(radius: String) {
                    editedShape.radius = radius.toInt()
                    editedShape.shouldRerender = true
                })

                elements[1].setValue(editedShape.width.toString(), fun(width: String) {
                    editedShape.width = width.toInt()
                    editedShape.shouldRerender = true
                })
                addRotationListeners(editedShape)
            }

            ShapeType.POLYGON -> {
                elements[0].setValue((editedShape as Polygon).radius.toString(), fun(radius: String) {
                    editedShape.radius = radius.toInt()
                    editedShape.shouldRerender = true
                })

                elements[1].setValue(editedShape.height.toString(), fun(height: String) {
                    editedShape.height = height.toInt()
                    editedShape.shouldRerender = true
                })

                elements[2].setValue(editedShape.sides.toString(), fun(sides: String) {
                    editedShape.sides = sides.toInt()
                    editedShape.shouldRerender = true
                })
                addRotationListeners(editedShape)
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

    private class ShapePropertyInput(textRenderer: TextRenderer, private val label: String, value: String) :
        Widget {

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
                return "Shape ${label.lowercase()} cannot be empty."
            }
            val value = input.text.toIntOrNull() ?: return "Shape ${label.lowercase()} must be a number."

            if (constraints.min > value) {
                return "Shape ${label.lowercase()} must be greater than or equal ${constraints.min}."
            }

            if (constraints.max != null && constraints.max!! < value) {
                return "Shape ${label.lowercase()} must be less than or equal ${constraints.max}."
            }
            return null
        }

        fun getLabel(): Label {
            return Label(label, input.x, input.y - 10)
        }

        fun getValue(): String {
            return input.text
        }

        /**
         * @param listener The listener that will be called when the value changes and **is valid**
         */
        fun setValue(value: String, listener: Consumer<String>) {
            input.text = value
            input.setChangedListener {
                if (checkForError() !== null) return@setChangedListener // Check for error first
                listener.accept(input.text)
            }
        }

        override fun setX(x: Int) {
            input.x = x
        }

        override fun setY(y: Int) {
            input.y = y
        }

        override fun getX(): Int {
            return input.x
        }

        override fun getY(): Int {
            return input.y
        }

        override fun getWidth(): Int {
            return 50
        }

        override fun getHeight(): Int {
            return 20
        }

        /**
         * @param min The minimum value for the input (inclusive)
         * @param max The maximum value for the input (inclusive). If null, there is no maximum.
         */
        data class Constraint(val min: Int, val max: Int?)

        override fun forEachChild(consumer: Consumer<ClickableWidget>) {
            consumer.accept(input)
        }
    }

    data class Label(val text: String, val x: Int, val y: Int)
}
