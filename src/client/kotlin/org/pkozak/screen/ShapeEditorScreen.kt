package org.pkozak.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
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
import java.awt.Color

class ShapeEditorScreen(private val parent: ShapesScreen, private val editedShape: Shape?) :
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
    private var sidesInput: TextFieldWidget? = null

    private var confirmBtn: ButtonWidget? = null
    private var centerBtn: IconButton? = null
    private var rotationButon: IconButton? = null

    private var errorText = ""
    private var shapeType: ShapeType = ShapeType.CUBE
    private var editorRotation = 0.0
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

        rotationButon = IconButton.Builder {
            editorRotation += 90
            editorRotation %= 360
            if (editedShape != null) {
//                editedShape.rotation.x = editorRotation.toInt()
                editedShape.rotation.y = editorRotation.toInt()
//                editedShape.rotation.z = editorRotation.toInt()
                editedShape.shouldRerender = true
                println("Rotation: ${editedShape.rotation}")
                editedShape.rotation.printMatrix()
            }
        }
            .dimensions(width / 3 + 108 + 20 + 108, 100, 20, 20)
            .tooltip(Tooltip.of(Text.literal("Rotate the shape around the Y axis")))
            .icon(Icons.ROTATE)
            .build()

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

        sidesInput =
            TextFieldWidget(client!!.textRenderer, width / 3 + 108 + 20 + 108, 100, 50, 20, Text.literal("Sides"))
        sidesInput!!.setPlaceholder(Text.literal("Sides"))
        sidesInput!!.text = "6"

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

            when (shapeType) {
                ShapeType.CUBE -> {
                    widthInput!!.text = (editedShape as Cube).dimensions.x.toString()
                    heightInput!!.text = editedShape.dimensions.y.toString()
                    depthInput!!.text = editedShape.dimensions.z.toString()
                }

                ShapeType.SPHERE -> {
                    radiusInput!!.text = (editedShape as Sphere).radius.toString()
                    radiusInput!!.setChangedListener {
                        onRadiusChange()
                    }
                }

                ShapeType.CYLINDER -> {
                    radiusInput!!.text = (editedShape as Cylinder).radius.toString()
                    heightInput!!.text = editedShape.height.toString()
                    radiusInput!!.setChangedListener {
                        onRadiusChange()
                    }
                    heightInput!!.setChangedListener {
                        onHeightChange()
                    }
                }

                ShapeType.TUNNEL -> {
                    radiusInput!!.text = (editedShape as Tunnel).radius.toString()
                    heightInput!!.text = editedShape.height.toString()
//                    rotationInput!!.setAngle(editedShape.rotation)
//                    editorRotation = editedShape.rotation
                    radiusInput!!.setChangedListener {
                        onRadiusChange()
                    }
                    heightInput!!.setChangedListener {
                        onHeightChange()
                    }
                }

                ShapeType.ARCH -> {
                    radiusInput!!.text = (editedShape as Arch).radius.toString()
                    heightInput!!.text = editedShape.width.toString()
//                    rotationInput!!.setAngle(editedShape.rotation)
//                    editorRotation = editedShape.rotation
                    radiusInput!!.setChangedListener {
                        onRadiusChange()
                    }
                    heightInput!!.setChangedListener {
                        onHeightChange()
                    }
                }

                ShapeType.POLYGON -> {
                    radiusInput!!.text = (editedShape as Polygon).radius.toString()
                    heightInput!!.text = editedShape.height.toString()
                    sidesInput!!.text = editedShape.sides.toString()
                    radiusInput!!.setChangedListener {
                        onRadiusChange()
                    }
                    heightInput!!.setChangedListener {
                        onHeightChange()
                    }
                    sidesInput!!.setChangedListener {
                        if (sidesInput!!.text.isEmpty()) return@setChangedListener
                        try {
                            sidesInput!!.text.toInt()
                        } catch (e: NumberFormatException) {
                            return@setChangedListener
                        }


                        editedShape.sides = sidesInput!!.text.toInt()
                        editedShape.shouldRerender = true
                    }
                }
            }
            onShapeTypeChange()
        } else {
            // Draw the inputs for the cube by default
            shapeTypeInput!!.active = true
            addDrawableChild(widthInput)
            addDrawableChild(depthInput)
            addDrawableChild(heightInput)
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

            ShapeType.POLYGON -> {
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

                context.drawText(
                    textRenderer,
                    "Sides",
                    sidesInput!!.x,
                    sidesInput!!.y - 10,
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
                Tunnel(name, color, pos, radius, height).apply {
                    this.rotation
                }
            }

            ShapeType.ARCH -> {
                val radius = radiusInput!!.text.toInt()
                val width = heightInput!!.text.toInt()
                Arch(name, color, pos, radius, width).apply {
//                    rotation = editorRotation
                }
            }

            ShapeType.POLYGON -> {
                val radius = radiusInput!!.text.toInt()
                val height = heightInput!!.text.toInt()
                val sides = sidesInput!!.text.toInt()
                Polygon(name, color, pos, radius, height, sides)
            }
        }

        parent.addShape(newShape)
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

        if (shapeType == ShapeType.CUBE) {
            if (widthInput!!.text.isEmpty() || heightInput!!.text.isEmpty() || depthInput!!.text.isEmpty()) {
                errorText = "Dimensions cannot be empty"
                return false
            }

            try {
                if (widthInput!!.text.toInt() <= 0 || heightInput!!.text.toInt() <= 0 || depthInput!!.text.toInt() <= 0) {
                    errorText = "Dimensions must be greater than 0"
                    return false
                }
            } catch (e: NumberFormatException) {
                errorText = "Dimensions must be numbers"
                return false
            }
        }

        if (shapeType == ShapeType.SPHERE) {
            if (radiusInput!!.text.isEmpty()) {
                errorText = "Radius cannot be empty"
                return false
            }

            try {
                if (radiusInput!!.text.toInt() <= 0) {
                    errorText = "Radius must be greater than 0"
                    return false
                }
            } catch (e: NumberFormatException) {
                errorText = "Radius must be a number"
                return false
            }
        }

        if (shapeType == ShapeType.CYLINDER || shapeType == ShapeType.POLYGON) {
            if (radiusInput!!.text.isEmpty() || heightInput!!.text.isEmpty()) {
                errorText = "Radius and height cannot be empty"
                return false
            }

            try {
                if (radiusInput!!.text.toInt() <= 0 || heightInput!!.text.toInt() <= 0) {
                    errorText = "Radius and height must be greater than 0"
                    return false
                }
            } catch (e: NumberFormatException) {
                errorText = "Radius and height must be numbers"
                return false
            }
        }

        if (shapeType == ShapeType.POLYGON) {
            if (sidesInput!!.text.isEmpty()) {
                errorText = "Sides cannot be empty"
                return false
            }

            try {
                if (sidesInput!!.text.toInt() < 3) {
                    errorText = "Polygon must have at least 3 sides"
                    return false
                }

                if (sidesInput!!.text.toInt() > 12) {
                    errorText = "Polygon cannot have more than 12 sides. Don't be ridiculous"
                    return false
                }

            } catch (e: NumberFormatException) {
                errorText = "Sides must be a number"
                return false
            }
        }

        if (shapeType == ShapeType.TUNNEL) {
            if (radiusInput!!.text.isEmpty() || heightInput!!.text.isEmpty()) {
                errorText = "Radius and length cannot be empty"
                return false
            }

            try {
                if (radiusInput!!.text.toInt() <= 0 || heightInput!!.text.toInt() <= 0) {
                    errorText = "Radius and length must be greater than 0"
                    return false
                }
            } catch (e: NumberFormatException) {
                errorText = "Radius and length must be numbers"
                return false
            }
        }

        if (shapeType == ShapeType.ARCH) {
            if (radiusInput!!.text.isEmpty() || heightInput!!.text.isEmpty()) {
                errorText = "Radius and width cannot be empty"
                return false
            }

            try {
                if (radiusInput!!.text.toInt() <= 0 || heightInput!!.text.toInt() <= 0) {
                    errorText = "Radius and width must be greater than 0"
                    return false
                }
            } catch (e: NumberFormatException) {
                errorText = "Radius and width must be numbers"
                return false
            }
        }

        errorText = ""
        return true
    }

    private fun onShapeTypeChange() {
        when (this.shapeType) {
            ShapeType.CUBE -> {
                addDrawableChild(widthInput)
                addDrawableChild(depthInput)
                addDrawableChild(heightInput)
                remove(radiusInput)
                remove(rotationButon)
                remove(sidesInput)
            }

            ShapeType.SPHERE -> {
                hideDimensionInputs()
                addDrawableChild(radiusInput)
                remove(rotationButon)
            }

            ShapeType.CYLINDER -> {
                hideDimensionInputs()
                remove(radiusInput)
                remove(rotationButon)
                addDrawableChild(radiusInput)
                addDrawableChild(heightInput)
            }

            ShapeType.TUNNEL, ShapeType.ARCH -> {
                hideDimensionInputs()
                remove(radiusInput)
                remove(heightInput)
                remove(rotationButon)
                addDrawableChild(radiusInput)
                addDrawableChild(heightInput)
                addDrawableChild(rotationButon)
            }

            ShapeType.POLYGON -> {
                hideDimensionInputs()
                remove(radiusInput)
                remove(heightInput)
                remove(rotationButon)
                addDrawableChild(radiusInput)
                addDrawableChild(heightInput)
                addDrawableChild(sidesInput)
            }
        }
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

        val vec = Vec3d(xCoordsInput!!.text.toDouble(), yCoordsInput!!.text.toDouble(), zCoordsInput!!.text.toDouble())
        editedShape?.pos = vec
        editedShape?.shouldRerender = true
    }

    private fun onColorChange() {
        val color = validateColor() ?: return
        editedShape?.color = color
        editedShape?.shouldRerender = true
    }

    private fun onRadiusChange() {
        if (radiusInput!!.text.isEmpty()) return
        try {
            radiusInput!!.text.toInt()
        } catch (e: NumberFormatException) {
            return
        }

        (editedShape as RadialShape).radius = radiusInput!!.text.toInt()
        editedShape.shouldRerender = true
    }

    private fun onHeightChange() {
        if (heightInput!!.text.isEmpty()) return
        try {
            heightInput!!.text.toInt()
        } catch (e: NumberFormatException) {
            return
        }

        // As much as I hate this, I have to do it for the type system
        // I could use a common class with height for all shapes but that would be even worse
        when (editedShape) {
            is Cylinder -> {
                editedShape.height = heightInput!!.text.toInt()
            }

            is Tunnel -> {
                editedShape.height = heightInput!!.text.toInt()
            }

            is Arch -> {
                editedShape.width = heightInput!!.text.toInt()
            }

            is Polygon -> {
                editedShape.height = heightInput!!.text.toInt()
            }
        }
        editedShape!!.shouldRerender = true
    }

    private fun hideDimensionInputs() {
        remove(widthInput)
        remove(heightInput)
        remove(depthInput)
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