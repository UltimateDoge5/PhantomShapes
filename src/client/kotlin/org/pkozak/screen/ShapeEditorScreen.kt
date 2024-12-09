package org.pkozak.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.SimplePositioningWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.pkozak.PhantomShapesClient
import org.pkozak.shape.*
import org.pkozak.ui.IconButton
import org.pkozak.ui.Icons
import org.pkozak.ui.input.ShapeInputGenerator
import org.pkozak.ui.input.ShapePropertyInput
import org.pkozak.ui.input.ShapePropertyInput.Constraint
import java.awt.Color

class ShapeEditorScreen(private val parent: ShapesScreen, private val editedShape: Shape?) :
    Screen(Text.literal("Shape editor")) {

    private var shapeType: ShapeType = ShapeType.CUBE
    private var shapePropertiesGenerator: ShapeInputGenerator? = null

    private var shapeNameInput: TextFieldWidget? = null
    private var shapeTypeInput: ButtonWidget? = null // OptionListWidget is too complex and not worth the time

    private var xCoordsInput: ShapePropertyInput? = null
    private var yCoordsInput: ShapePropertyInput? = null
    private var zCoordsInput: ShapePropertyInput? = null

    private var redInput: ShapePropertyInput? = null
    private var greenInput: ShapePropertyInput? = null
    private var blueInput: ShapePropertyInput? = null

    private var confirmBtn: ButtonWidget? = null
    private var centerBtn: IconButton? = null

    private var errorText = ""
    private var originalShape = editedShape?.toJsonObject()

    override fun init() {
        super.init()

        shapeNameInput =
            TextFieldWidget(client!!.textRenderer, width / 3 - 25 - 54, 60, 52 * 3, 20, Text.literal("Shape name"))
        shapeNameInput!!.setPlaceholder(Text.literal("Shape name"))
        shapeNameInput!!.text = "New shape ${parent.shapes.size + 1}"


        // For the rest of the inputs, use property inputs instead of text ones.
        // Property inputs handle scroll input and automatically validate inputs
        // We don't have to use the labels at all
        xCoordsInput =
            ShapePropertyInput(client!!.textRenderer, width / 3 - 25 - 54, 100, "X coordinate")
        xCoordsInput!!.setPlaceholder(Text.literal("X"))
        xCoordsInput!!.text = client?.player?.x?.toInt().toString()
        xCoordsInput!!.constraints = null

        yCoordsInput =
            ShapePropertyInput(client!!.textRenderer, width / 3 - 25, 100, "Y coordinate")
        yCoordsInput!!.setPlaceholder(Text.literal("Y"))
        yCoordsInput!!.text = client?.player?.y?.toInt().toString()
        yCoordsInput!!.constraints = null

        zCoordsInput =
            ShapePropertyInput(client!!.textRenderer, width / 3 - 25 + 54, 100, "Z coordinate")
        zCoordsInput!!.setPlaceholder(Text.literal("Z"))
        zCoordsInput!!.text = client?.player?.z?.toInt().toString()
        zCoordsInput!!.constraints = null

        // Color inputs
        redInput = ShapePropertyInput(client!!.textRenderer, width / 3 - 25 - 54, 140, "Red")
        redInput!!.setPlaceholder(Text.literal("Red"))
        redInput!!.text = "0"
        redInput!!.constraints = Constraint(0, 255)

        greenInput = ShapePropertyInput(client!!.textRenderer, width / 3 - 25, 140, "Green")
        greenInput!!.setPlaceholder(Text.literal("Green"))
        greenInput!!.text = "255"
        greenInput!!.constraints = Constraint(0, 255)


        blueInput = ShapePropertyInput(client!!.textRenderer, width / 3 - 25 + 54, 140, "Blue")
        blueInput!!.setPlaceholder(Text.literal("Blue"))
        blueInput!!.text = "255"
        blueInput!!.constraints = Constraint(0, 255)

        confirmBtn = ButtonWidget.builder(Text.literal("Confirm")) {
            if (editedShape == null) createShape()
            close()
        }
            .dimensions(width / 2 - 205, 200, 200, 20).build()

        val cancelBtn = ButtonWidget.builder(Text.literal("Cancel")) {
            if (editedShape != null) {
                val reconstructedShape = Shape.fromJsonObject(originalShape!!)
                parent.shapes[parent.shapes.indexOf(editedShape)] = reconstructedShape
                PhantomShapesClient.cleanupBuffers()
            }
            close()
        }
            .dimensions(width / 2 + 5, 200, 200, 20).build()

        centerBtn = IconButton.Builder {
            xCoordsInput!!.text = client?.player?.x?.toInt().toString()
            yCoordsInput!!.text = client?.player?.y?.toInt().toString()
            zCoordsInput!!.text = client?.player?.z?.toInt().toString()
        }
            .dimensions(width / 3 - 25 + 108, 100, 20, 20)
            .tooltip(Tooltip.of(Text.literal("Center the shape on player position")))
            .icon(Icons.PIN_ICON)
            .build()

        shapeTypeInput = ButtonWidget.builder(Text.literal("Cube")) {
            when (shapeTypeInput!!.message.asTruncatedString(8).lowercase()) {
                "cube" -> {
                    shapeTypeInput!!.message = Text.literal("Sphere")
                    shapeType = ShapeType.SPHERE
                }

                "sphere" -> {
                    shapeTypeInput!!.message = Text.literal("Cylinder")
                    shapeType = ShapeType.CYLINDER
                }

                "cylinder" -> {
                    shapeTypeInput!!.message = Text.literal("Arch")
                    shapeType = ShapeType.ARCH
                }

                "arch" -> {
                    shapeTypeInput!!.message = Text.literal("Polygon")
                    shapeType = ShapeType.POLYGON
                }

                "polygon" -> {
                    shapeTypeInput!!.message = Text.literal("Torus")
                    shapeType = ShapeType.TORUS
                }

                "torus" -> {
                    shapeTypeInput!!.message = Text.literal("Cube")
                    shapeType = ShapeType.CUBE
                }
            }

            shapePropertiesGenerator!!.shapeType = shapeType
            drawShapePropertiesInputs()
        }
            .dimensions(width / 3 + 108 + 20, 60, 60, 20).build()

        shapePropertiesGenerator = ShapeInputGenerator(client!!.textRenderer)

        // If the shape is not null, it means we are editing an existing shape
        if (editedShape != null) {
            shapeNameInput!!.text = editedShape.name
            xCoordsInput!!.text = editedShape.pos.x.toInt().toString()
            yCoordsInput!!.text = editedShape.pos.y.toInt().toString()
            zCoordsInput!!.text = editedShape.pos.z.toInt().toString()

            redInput!!.text = editedShape.color.red.toString()
            greenInput!!.text = editedShape.color.green.toString()
            blueInput!!.text = editedShape.color.blue.toString()

            shapeType = editedShape.type
            shapeTypeInput!!.message = Text.literal(shapeType.name.uppercase())
            shapeTypeInput!!.active = false

            // Name change listener
            shapeNameInput!!.setChangedListener {
                if (shapeNameInput!!.text.isEmpty()) return@setChangedListener
                if (parent.shapes.any { it.name == shapeNameInput!!.text }) {
                    errorText = "Shape with this name already exists"
                    return@setChangedListener
                }

                errorText = ""
                editedShape.name = shapeNameInput!!.text
                editedShape.shouldRerender = true
                PhantomShapesClient.cleanupBuffers()
            }

            // Setup listeners for live position updates
            xCoordsInput!!.setChangedListener { onCoordinateChange() }
            yCoordsInput!!.setChangedListener { onCoordinateChange() }
            zCoordsInput!!.setChangedListener { onCoordinateChange() }

            // Color listeners
            redInput!!.setChangedListener { onColorChange() }
            greenInput!!.setChangedListener { onColorChange() }
            blueInput!!.setChangedListener { onColorChange() }

            shapePropertiesGenerator!!.grid.forEachChild(::remove)
            val inputs = shapePropertiesGenerator!!.editShape(editedShape)
            SimplePositioningWidget.setPos(inputs, width / 3 + 108 + 20, 100, this.width / 2, this.height / 2, 0f, 0f)
            inputs.refreshPositions()
            inputs.forEachChild(::addDrawableChild)
        } else {
            // Draw the inputs for the cube by default
            shapeTypeInput!!.active = true
            drawShapePropertiesInputs()
        }

        addDrawableChild(confirmBtn)
        addDrawableChild(cancelBtn)

        addDrawableChild(centerBtn)
        addDrawableChild(shapeNameInput)
        addDrawableChild(shapeTypeInput)
        addDrawableChild(xCoordsInput)
        addDrawableChild(yCoordsInput)
        addDrawableChild(zCoordsInput)
        addDrawableChild(redInput)
        addDrawableChild(greenInput)
        addDrawableChild(blueInput)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(
            textRenderer,
            if (editedShape != null) "Editing shape - ${editedShape.name}" else "Create a new shape",
            width / 2,
            20,
            0xFFFFFF
        )

        context.drawText(
            textRenderer,
            "Shape name",
            shapeNameInput!!.x,
            shapeNameInput!!.y - 10,
            0xFFFFFF,
            true
        )

        context.drawText(
            textRenderer,
            "Position",
            xCoordsInput!!.x,
            xCoordsInput!!.y - 10,
            0xFFFFFF,
            true
        )

        context.drawText(
            textRenderer,
            "Color",
            redInput!!.x,
            redInput!!.y - 10,
            0xFFFFFF,
            true
        )

        // Render shape color
        drawColorBox(context)

        context.drawText(
            textRenderer,
            "Shape type",
            shapeTypeInput!!.x,
            shapeTypeInput!!.y - 10,
            0xFFFFFF,
            true
        )

        // Render the shape type next to the button
        context.drawGuiTexture(
            RenderLayer::getGuiTextured,
            Shape.getIcon(shapeType),
            shapeTypeInput!!.x + 62,
            shapeTypeInput!!.y - 1,
            20,
            20
        )

        validateShape()

        if (errorText.isNotEmpty()) {
            context.drawCenteredTextWithShadow(
                textRenderer,
                errorText,
                width / 2,
                180,
                Colors.LIGHT_RED
            )
        }

        confirmBtn!!.active = errorText.isEmpty()
        val labels = shapePropertiesGenerator!!.getLabels()
        for (label in labels) {
            context.drawText(
                textRenderer,
                label.text,
                label.x,
                label.y,
                0xFFFFFF,
                true
            )
        }
    }

    private fun drawShapePropertiesInputs() {
        shapePropertiesGenerator!!.grid.forEachChild(::remove)
        val inputs = shapePropertiesGenerator!!.generateInputs()
        SimplePositioningWidget.setPos(inputs, width / 3 + 108 + 20, 100, width / 2, height / 2, 0f, 0f)
        inputs.refreshPositions()
        inputs.forEachChild(::addDrawableChild)
    }

    private fun drawColorBox(context: DrawContext) {
        context.drawBorder(blueInput!!.x + 54, blueInput!!.y, 20, 20, ColorHelper.getArgb(200, 255, 255, 255))

        val color = validateColor() ?: return
        context.fill(blueInput!!.x + 55, blueInput!!.y + 1, blueInput!!.x + 55 + 18, blueInput!!.y + 19, color.rgb)
    }

    private fun createShape() {
        val name = shapeNameInput!!.text
        val x = xCoordsInput!!.text.toDouble()
        val y = yCoordsInput!!.text.toDouble()
        val z = zCoordsInput!!.text.toDouble()

        val pos = Vec3d(x, y, z)
        val color = Color(redInput!!.text.toInt(), greenInput!!.text.toInt(), blueInput!!.text.toInt())

        // Create a shape with arbitrary properties as it then gets overwritten via the generator
        when (shapeType) {
            ShapeType.CUBE -> {
                val shape = shapePropertiesGenerator!!.getShapeProperties(Cube(name, color, pos, Vec3i.ZERO))
                parent.addShape(shape)
            }

            ShapeType.SPHERE -> {
                val shape = shapePropertiesGenerator!!.getShapeProperties(Sphere(name, color, pos, 1))
                parent.addShape(shape)
            }

            ShapeType.CYLINDER -> {
                val shape = shapePropertiesGenerator!!.getShapeProperties(Cylinder(name, color, pos, 1, 1))
                parent.addShape(shape)
            }

            ShapeType.ARCH -> {
                val shape = shapePropertiesGenerator!!.getShapeProperties(Arch(name, color, pos, 1, 1))
                parent.addShape(shape)
            }

            ShapeType.POLYGON -> {
                val shape = shapePropertiesGenerator!!.getShapeProperties(Polygon(name, color, pos, 1, 1, 1))
                parent.addShape(shape)
            }

            ShapeType.TORUS -> {
                val shape = shapePropertiesGenerator!!.getShapeProperties(Torus(name, color, pos, 1, 1))
                parent.addShape(shape)
            }
        }
    }

    private fun validateShape(): Boolean {
        if (shapeNameInput!!.text.isEmpty()) {
            errorText = "Shape name cannot be empty"
            return false
        }

        if (shapeNameInput!!.text.length > 16) {
            errorText = "Shape name cannot be longer than 16 characters"
            return false
        }

        if (editedShape == null && parent.shapes.any { it.name == shapeNameInput!!.text }) {
            errorText = "Shape with this name already exists"
            return false
        }

        // This could be technically removed but I prefere these error messages than the input automated ones
        if (xCoordsInput!!.text.isEmpty() || yCoordsInput!!.text.isEmpty() || zCoordsInput!!.text.isEmpty()) {
            errorText = "Coordinates cannot be empty"
            return false
        }

        if (redInput!!.text.isEmpty() || greenInput!!.text.isEmpty() || blueInput!!.text.isEmpty()) {
            errorText = "Color cannot be empty"
            return false
        }

        try {
            redInput!!.text.toInt()
            greenInput!!.text.toInt()
            blueInput!!.text.toInt()
        } catch (e: NumberFormatException) {
            errorText = "Color values must be numbers"
            return false
        }

        if (redInput!!.text.toInt() !in 0..255 || greenInput!!.text.toInt() !in 0..255 || blueInput!!.text.toInt() !in 0..255) {
            errorText = "Color values must be between 0 and 255"
            return false
        }

        val parametersError = shapePropertiesGenerator!!.checkForError()
        if (parametersError != null) {
            errorText = parametersError
            return false
        }

        errorText = ""
        return true
    }

    private fun onCoordinateChange() {
        val vec = Vec3d(xCoordsInput!!.text.toDouble(), yCoordsInput!!.text.toDouble(), zCoordsInput!!.text.toDouble())
        editedShape?.pos = vec
        editedShape?.shouldRerender = true
    }

    private fun onColorChange() {
        val color = validateColor() ?: return
        editedShape?.color = color
        editedShape?.shouldRerender = true
    }

    private fun validateColor(): Color? {
        if (redInput!!.checkForError() !== null || greenInput!!.checkForError() !== null || blueInput!!.checkForError() !== null) {
            return null
        }

        val color = Color(redInput!!.text.toInt(), greenInput!!.text.toInt(), blueInput!!.text.toInt())
        return color
    }

    override fun close() {
        client?.setScreen(parent)
    }
}