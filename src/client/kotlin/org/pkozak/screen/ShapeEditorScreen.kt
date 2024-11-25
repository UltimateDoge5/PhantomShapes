package org.pkozak.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.GridWidget
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
import org.pkozak.ui.ShapePropertyInputGenerator
import java.awt.Color

class ShapeEditorScreen(private val parent: ShapesScreen, private val editedShape: Shape?) :
    Screen(Text.literal("Shape editor")) {
    private var grid: GridWidget = GridWidget().setColumnSpacing(10).setRowSpacing(8)
    private var adder = grid.createAdder(8)

    private var shapeType: ShapeType = ShapeType.CUBE
    private var shapePropertiesGenerator: ShapePropertyInputGenerator? = null

    private var shapeNameInput: TextFieldWidget? = null
    private var shapeTypeInput: ButtonWidget? = null // OptionListWidget is too complex and not worth the time

    private var xCoordsInput: TextFieldWidget? = null
    private var yCoordsInput: TextFieldWidget? = null
    private var zCoordsInput: TextFieldWidget? = null

    private var redInput: TextFieldWidget? = null
    private var greenInput: TextFieldWidget? = null
    private var blueInput: TextFieldWidget? = null

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

        xCoordsInput =
            TextFieldWidget(client!!.textRenderer, width / 3 - 25 - 54, 100, 50, 20, Text.literal("X coordinate"))
        xCoordsInput!!.setPlaceholder(Text.literal("X"))
        xCoordsInput!!.text = client?.player?.x?.toInt().toString()

        yCoordsInput =
            TextFieldWidget(client!!.textRenderer, width / 3 - 25, 100, 50, 20, Text.literal("Y coordinate"))
        yCoordsInput!!.setPlaceholder(Text.literal("Y"))
        yCoordsInput!!.text = client?.player?.y?.toInt().toString()

        zCoordsInput =
            TextFieldWidget(client!!.textRenderer, width / 3 - 25 + 54, 100, 50, 20, Text.literal("Z coordinate"))
        zCoordsInput!!.setPlaceholder(Text.literal("Z"))
        zCoordsInput!!.text = client?.player?.z?.toInt().toString()

        // Color inputs
        redInput = TextFieldWidget(client!!.textRenderer, width / 3 - 25 - 54, 140, 50, 20, Text.literal("Red"))
        redInput!!.setPlaceholder(Text.literal("Red"))
        redInput!!.text = "0"

        greenInput = TextFieldWidget(client!!.textRenderer, width / 3 - 25, 140, 50, 20, Text.literal("Green"))
        greenInput!!.setPlaceholder(Text.literal("Green"))
        greenInput!!.text = "255"

        blueInput = TextFieldWidget(client!!.textRenderer, width / 3 - 25 + 54, 140, 50, 20, Text.literal("Blue"))
        blueInput!!.setPlaceholder(Text.literal("Blue"))
        blueInput!!.text = "255"


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
                    shapeTypeInput!!.message = Text.literal("Tunnel")
                    shapeType = ShapeType.TUNNEL
                }

                "tunnel" -> {
                    shapeTypeInput!!.message = Text.literal("Arch")
                    shapeType = ShapeType.ARCH
                }

                "arch" -> {
                    shapeTypeInput!!.message = Text.literal("Hexagon")
                    shapeType = ShapeType.POLYGON
                }

                "hexagon" -> {
                    shapeTypeInput!!.message = Text.literal("Cube")
                    shapeType = ShapeType.CUBE
                }
            }

            shapePropertiesGenerator!!.shapeType = shapeType
            drawShapePropertiesInputs()
        }
            .dimensions(width / 3 + 108 + 20, 60, 60, 20).build()

        shapePropertiesGenerator = ShapePropertyInputGenerator(client!!.textRenderer)

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
            SimplePositioningWidget.setPos(inputs, width / 2, 160, this.width / 2, this.height / 2, 0.5f, 0f)
            inputs.refreshPositions()
            inputs.forEachChild(::addDrawableChild)
        } else {
            // Draw the inputs for the cube by default
            shapeTypeInput!!.active = true
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

        drawShapePropertiesInputs()
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
            context.drawText(
                textRenderer,
                errorText,
                width / 3 + 108 + 20,
                152,
                Colors.LIGHT_RED,
                true
            )
        }

        confirmBtn!!.active = errorText.isEmpty()
    }

    private fun drawShapePropertiesInputs() {
        shapePropertiesGenerator!!.grid.forEachChild(::remove)
        val inputs = shapePropertiesGenerator!!.generateInputs()
        SimplePositioningWidget.setPos(inputs, width / 2, 160, this.width / 2, this.height / 2, 0.5f, 0f)
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

            ShapeType.TUNNEL -> {
                val shape = shapePropertiesGenerator!!.getShapeProperties(Tunnel(name, color, pos, 1, 1))
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
        if (xCoordsInput!!.text.isEmpty() || yCoordsInput!!.text.isEmpty() || zCoordsInput!!.text.isEmpty()) {
            return
        }

        try {
            xCoordsInput!!.text.toDouble()
            yCoordsInput!!.text.toDouble()
            zCoordsInput!!.text.toDouble()
        } catch (e: NumberFormatException) {
            return
        }

        val vec =
            Vec3d(xCoordsInput!!.text.toDouble(), yCoordsInput!!.text.toDouble(), zCoordsInput!!.text.toDouble())
        editedShape?.pos = vec
        editedShape?.shouldRerender = true
    }

    private fun onColorChange() {
        val color = validateColor() ?: return
        editedShape?.color = color
        editedShape?.shouldRerender = true
    }

    private fun validateColor(): Color? {
        if (redInput!!.text.isEmpty() || greenInput!!.text.isEmpty() || blueInput!!.text.isEmpty()) {
            return null
        }

        try {
            redInput!!.text.toInt()
            greenInput!!.text.toInt()
            blueInput!!.text.toInt()
        } catch (e: NumberFormatException) {
            return null
        }

        if (redInput!!.text.toInt() !in 0..255 || greenInput!!.text.toInt() !in 0..255 || blueInput!!.text.toInt() !in 0..255) {
            return null
        }

        val color = Color(redInput!!.text.toInt(), greenInput!!.text.toInt(), blueInput!!.text.toInt())
        return color
    }

    override fun close() {
        client?.setScreen(parent)
    }
}