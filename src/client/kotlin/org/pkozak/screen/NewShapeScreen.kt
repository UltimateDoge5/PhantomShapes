package org.pkozak.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Colors
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.pkozak.Shape
import org.pkozak.ShapeType
import org.pkozak.shape.*
import org.pkozak.ui.RotationSlider
import java.awt.Color

class NewShapeScreen(private val parent: ShapesScreen, private val editedShape: Shape?) :
    Screen(Text.literal("Shape editor")) {
    private var shapeNameInput: TextFieldWidget? = null
    private var shapeTypeInput: ButtonWidget? = null // OptionListWidget is too complex and not worth the time

    private var xCoordsInput: TextFieldWidget? = null
    private var yCoordsInput: TextFieldWidget? = null
    private var zCoordsInput: TextFieldWidget? = null

    private var redInput: TextFieldWidget? = null
    private var greenInput: TextFieldWidget? = null
    private var blueInput: TextFieldWidget? = null

    private var widthInput: TextFieldWidget? = null
    private var heightInput: TextFieldWidget? = null
    private var depthInput: TextFieldWidget? = null
    private var radiusInput: TextFieldWidget? = null

    private var confirmBtn: ButtonWidget? = null
    private var rotationInput: RotationSlider? = null

    private var errorText = ""
    private var shapeType: ShapeType = ShapeType.CUBE
    private var rotation = 0.0

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

        confirmBtn = ButtonWidget.builder(Text.literal("Confirm")) { createOrEditShape() }
            .dimensions(width / 2 - 100, 180, 200, 20).build()


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
                    shapeTypeInput!!.message = Text.literal("Cube")
                    shapeType = ShapeType.CUBE
                }
            }
            onShapeTypeChange()
        }
            .dimensions(width / 3 + 108 + 20, 60, 60, 20).build()

        widthInput = TextFieldWidget(client!!.textRenderer, width / 3 + 108 + 20, 100, 50, 20, Text.literal("Width"))
        widthInput!!.setPlaceholder(Text.literal("Width"))
        widthInput!!.text = "3"

        heightInput =
            TextFieldWidget(client!!.textRenderer, width / 3 + 108 + 20 + 54, 100, 50, 20, Text.literal("Height"))
        heightInput!!.setPlaceholder(Text.literal("Height"))
        heightInput!!.text = "3"

        depthInput =
            TextFieldWidget(client!!.textRenderer, width / 3 + 108 + 20 + 108, 100, 50, 20, Text.literal("Depth"))
        depthInput!!.setPlaceholder(Text.literal("Depth"))
        depthInput!!.text = "3"

        radiusInput =
            TextFieldWidget(client!!.textRenderer, width / 3 + 108 + 20, 100, 50, 20, Text.literal("Radius"))
        radiusInput!!.setPlaceholder(Text.literal("Radius"))
        radiusInput!!.text = "5"

        rotationInput = RotationSlider(width / 3 + 108 + 20 + 108, 100, 75, 20) { value: Double ->
            if (editedShape != null) {
                editedShape.rotation = value
            }
        }

        // If shape is not null, it means we are editing an existing shape
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

            when (shapeType) {
                ShapeType.CUBE -> {
                    widthInput!!.text = (editedShape as Cube).dimensions.x.toString()
                    heightInput!!.text = editedShape.dimensions.y.toString()
                    depthInput!!.text = editedShape.dimensions.z.toString()
                }

                ShapeType.SPHERE -> {
                    radiusInput!!.text = (editedShape as Sphere).radius.toString()
                }

                ShapeType.CYLINDER -> {
                    radiusInput!!.text = (editedShape as Cylinder).radius.toString()
                    heightInput!!.text = editedShape.height.toString()
                }

                ShapeType.TUNNEL -> {
                    radiusInput!!.text = (editedShape as Tunnel).radius.toString()
                    heightInput!!.text = editedShape.height.toString()
                    rotationInput!!.setAngle(editedShape.rotation)
                }

                ShapeType.ARCH -> {
                    radiusInput!!.text = (editedShape as ArchBridge).radius.toString()
                    heightInput!!.text = editedShape.width.toString()
                    rotationInput!!.setAngle(editedShape.rotation)
                }
            }

            onShapeTypeChange()
        }

        addDrawableChild(confirmBtn)
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
            Shape.getIcon(shapeType),
            shapeTypeInput!!.x + 62,
            shapeTypeInput!!.y - 1,
            20,
            20
        )

        when (shapeType) {
            ShapeType.CUBE -> {
                context.drawText(
                    textRenderer,
                    "Dimensions",
                    widthInput!!.x,
                    widthInput!!.y - 10,
                    0xFFFFFF,
                    true
                )
            }

            ShapeType.SPHERE -> {
                context.drawText(
                    textRenderer,
                    "Radius",
                    radiusInput!!.x,
                    radiusInput!!.y - 10,
                    0xFFFFFF,
                    true
                )
            }

            ShapeType.CYLINDER -> {
                context.drawText(
                    textRenderer,
                    "Radius",
                    radiusInput!!.x,
                    radiusInput!!.y - 10,
                    0xFFFFFF,
                    true
                )

                context.drawText(
                    textRenderer,
                    "Height",
                    heightInput!!.x,
                    heightInput!!.y - 10,
                    0xFFFFFF,
                    true
                )
            }

            ShapeType.TUNNEL -> {
                context.drawText(
                    textRenderer,
                    "Radius",
                    radiusInput!!.x,
                    radiusInput!!.y - 10,
                    0xFFFFFF,
                    true
                )

                context.drawText(
                    textRenderer,
                    "Length",
                    heightInput!!.x,
                    heightInput!!.y - 10,
                    0xFFFFFF,
                    true
                )
            }

            ShapeType.ARCH -> {
                context.drawText(
                    textRenderer,
                    "Radius",
                    radiusInput!!.x,
                    radiusInput!!.y - 10,
                    0xFFFFFF,
                    true
                )

                context.drawText(
                    textRenderer,
                    "Width",
                    heightInput!!.x,
                    heightInput!!.y - 10,
                    0xFFFFFF,
                    true
                )
            }
        }

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

    private fun drawColorBox(context: DrawContext) {
        context.drawBorder(blueInput!!.x + 54, blueInput!!.y, 20, 20, ColorHelper.Argb.getArgb(200, 255, 255, 255))

        if (redInput!!.text.isEmpty() || greenInput!!.text.isEmpty() || blueInput!!.text.isEmpty()) {
            return
        }

        val color = Color(redInput!!.text.toInt(), greenInput!!.text.toInt(), blueInput!!.text.toInt())
        context.fill(blueInput!!.x + 55, blueInput!!.y + 1, blueInput!!.x + 55 + 18, blueInput!!.y + 19, color.rgb)
    }

    private fun createOrEditShape() {
        val name = shapeNameInput!!.text
        val x = xCoordsInput!!.text.toDouble()
        val y = yCoordsInput!!.text.toDouble()
        val z = zCoordsInput!!.text.toDouble()

        val pos = Vec3d(x, y, z)
        val color = Color(redInput!!.text.toInt(), greenInput!!.text.toInt(), blueInput!!.text.toInt())

        val newShape = when (this.shapeType) {
            ShapeType.CUBE -> {
                val width = widthInput!!.text.toInt()
                val height = heightInput!!.text.toInt()
                val depth = depthInput!!.text.toInt()
                val dimensions = Vec3i(width, height, depth)
                Cube(name, color, pos, dimensions)
            }

            ShapeType.SPHERE -> {
                val radius = radiusInput!!.text.toInt()
                Sphere(name, color, pos, radius)
            }

            ShapeType.CYLINDER -> {
                val radius = radiusInput!!.text.toInt()
                val height = heightInput!!.text.toInt()
                Cylinder(name, color, pos, radius, height)
            }

            ShapeType.TUNNEL -> {
                val radius = radiusInput!!.text.toInt()
                val height = heightInput!!.text.toInt()
                Tunnel(name, color, pos, radius, height).apply{
                    rotation = rotationInput!!.getAngle()
                }
            }

            ShapeType.ARCH -> {
                val radius = radiusInput!!.text.toInt()
                val width = heightInput!!.text.toInt()
                ArchBridge(name, color, pos, radius, width).apply{
                    rotation = rotationInput!!.getAngle()
                }
            }
        }

        if (editedShape !== null) {
            val shapeIndex = parent.shapes.indexOfFirst { it.name == editedShape.name }
            if (shapeIndex == -1) {
                errorText = "Shape could not be edited"
                return
            }
            parent.shapes[shapeIndex] = newShape
        } else {
            parent.addShape(newShape)
        }
        close()
    }

    private fun validateShape() {
        if (shapeNameInput!!.text.isEmpty()) {
            errorText = "Shape name cannot be empty"
            return
        }

        if (shapeNameInput!!.text.length > 16) {
            errorText = "Shape name cannot be longer than 16 characters"
            return
        }

        if (editedShape == null && parent.shapes.any { it.name == shapeNameInput!!.text }) {
            errorText = "Shape with this name already exists"
            return
        }

        if (xCoordsInput!!.text.isEmpty() || yCoordsInput!!.text.isEmpty() || zCoordsInput!!.text.isEmpty()) {
            errorText = "Coordinates cannot be empty"
            return
        }

        if (redInput!!.text.isEmpty() || greenInput!!.text.isEmpty() || blueInput!!.text.isEmpty()) {
            errorText = "Color cannot be empty"
            return
        }

        if (shapeType == ShapeType.CUBE) {
            if (widthInput!!.text.isEmpty() || heightInput!!.text.isEmpty() || depthInput!!.text.isEmpty()) {
                errorText = "Dimensions cannot be empty"
                return
            }

            try {
                if (widthInput!!.text.toInt() <= 0 || heightInput!!.text.toInt() <= 0 || depthInput!!.text.toInt() <= 0) {
                    errorText = "Dimensions must be greater than 0"
                    return
                }
            } catch (e: NumberFormatException) {
                errorText = "Dimensions must be numbers"
                return
            }
        }

        if (shapeType == ShapeType.SPHERE) {
            if (radiusInput!!.text.isEmpty()) {
                errorText = "Radius cannot be empty"
                return
            }

            try {
                if (radiusInput!!.text.toInt() <= 0) {
                    errorText = "Radius must be greater than 0"
                    return
                }
            } catch (e: NumberFormatException) {
                errorText = "Radius must be a number"
                return
            }
        }

        if (shapeType == ShapeType.CYLINDER) {
            if (radiusInput!!.text.isEmpty() || heightInput!!.text.isEmpty()) {
                errorText = "Radius and height cannot be empty"
                return
            }

            try {
                if (radiusInput!!.text.toInt() <= 0 || heightInput!!.text.toInt() <= 0) {
                    errorText = "Radius and height must be greater than 0"
                    return
                }
            } catch (e: NumberFormatException) {
                errorText = "Radius and height must be numbers"
                return
            }
        }

        errorText = ""
    }

    private fun onShapeTypeChange() {
        when (this.shapeType) {
            ShapeType.CUBE -> {
                addDrawableChild(widthInput)
                addDrawableChild(depthInput)
                addDrawableChild(heightInput)
                remove(radiusInput)
                remove(rotationInput)
            }

            ShapeType.SPHERE -> {
                hideDimensionInputs()
                addDrawableChild(radiusInput)
                remove(rotationInput)
            }

            ShapeType.CYLINDER -> {
                hideDimensionInputs()
                remove(radiusInput)
                remove(rotationInput)
                addDrawableChild(radiusInput)
                addDrawableChild(heightInput)
            }

            ShapeType.TUNNEL, ShapeType.ARCH -> {
                hideDimensionInputs()
                remove(radiusInput)
                remove(heightInput)
                remove(rotationInput)
                addDrawableChild(radiusInput)
                addDrawableChild(heightInput)
                addDrawableChild(rotationInput)
            }
        }
    }

    private fun hideDimensionInputs() {
        remove(widthInput)
        remove(heightInput)
        remove(depthInput)
    }

    override fun close() {
        client?.setScreen(parent)
    }
}