package org.pkozak.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.Vec3d
import org.pkozak.PhantomShapesClient.logger
import org.pkozak.ShapeType
import org.pkozak.shape.Cube
import org.pkozak.shape.Cylinder
import org.pkozak.shape.Sphere
import java.awt.Color

class NewShapeScreen(private val parent: ShapesScreen) : Screen(Text.literal("Shapes manager")) {
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

    private var shapeType: ShapeType = ShapeType.CUBE


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

        confirmBtn = ButtonWidget.builder(Text.literal("Confirm")) { createShape() }
            .dimensions(width / 2 - 100, 180, 200, 20).build()


        shapeTypeInput = ButtonWidget.builder(Text.literal("Cube")) {
            logger.info("Shape type changed ${shapeTypeInput!!.message.asTruncatedString(8).lowercase()}")
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
                    shapeTypeInput!!.message = Text.literal("Cube")
                    shapeType = ShapeType.CUBE
                }
            }
            onShapeTypeChange()
        }
            .dimensions(width / 3 + 108 + 20, 60, 60, 20)
            .build()

        widthInput = TextFieldWidget(client!!.textRenderer, width / 3 + 108 + 20, 100, 50, 20, Text.literal("Width"))
        heightInput =
            TextFieldWidget(client!!.textRenderer, width / 3 + 108 + 20 + 54, 100, 50, 20, Text.literal("Height"))
        depthInput =
            TextFieldWidget(client!!.textRenderer, width / 3 + 108 + 20 + 108, 100, 50, 20, Text.literal("Depth"))

        radiusInput =
            TextFieldWidget(client!!.textRenderer, width / 3 + 108 + 20, 100, 50, 20, Text.literal("Radius"))

        showDimensionInputs()
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
            "Create a new shape",
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
        drawColor(context)

        context.drawText(
            textRenderer,
            "Shape type",
            shapeTypeInput!!.x,
            shapeTypeInput!!.y - 10,
            0xFFFFFF,
            true
        )

        when (shapeTypeInput!!.message.asTruncatedString(8).lowercase()) {
            "cube" -> {
                context.drawText(
                    textRenderer,
                    "Dimensions",
                    widthInput!!.x,
                    widthInput!!.y - 10,
                    0xFFFFFF,
                    true
                )
            }

            "sphere" -> {
                context.drawText(
                    textRenderer,
                    "Radius",
                    radiusInput!!.x,
                    radiusInput!!.y - 10,
                    0xFFFFFF,
                    true
                )
            }

            "cylinder" -> {
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
        }
    }

    private fun drawColor(context: DrawContext) {
        context.drawBorder(blueInput!!.x + 54, blueInput!!.y, 20, 20, ColorHelper.Argb.getArgb(200, 255, 255, 255))

        if (redInput!!.text.isEmpty() || greenInput!!.text.isEmpty() || blueInput!!.text.isEmpty()) {
            return
        }

        val color = Color(redInput!!.text.toInt(), greenInput!!.text.toInt(), blueInput!!.text.toInt())
        context.fill(blueInput!!.x + 55, blueInput!!.y + 1, blueInput!!.x + 55 + 18, blueInput!!.y + 19, color.rgb)
    }

    private fun createShape() {
        val x = xCoordsInput!!.text.toDouble()
        val y = yCoordsInput!!.text.toDouble()
        val z = zCoordsInput!!.text.toDouble()

        val pos = Vec3d(x, y, z)
        val color = Color(redInput!!.text.toInt(), greenInput!!.text.toInt(), blueInput!!.text.toInt())

        val shape = when (shapeTypeInput!!.message.asTruncatedString(8).lowercase()) {
            "cube" -> {
                val width = widthInput!!.text.toDouble()
                val height = heightInput!!.text.toDouble()
                val depth = depthInput!!.text.toDouble()
                val dimensions = Vec3d(width, height, depth)
                Cube(color, pos, true, dimensions)
            }

            "sphere" -> {
                val radius = radiusInput!!.text.toInt()
                Sphere(color, pos, true, radius)
            }

            "cylinder" -> {
                val radius = radiusInput!!.text.toInt()
                val height = heightInput!!.text.toInt()
                Cylinder(color, pos, radius, height)
            }

            else -> throw IllegalArgumentException("Invalid shape type")
        }

        parent.addShape(shape)
    }

    private fun onShapeTypeChange() {
        when (this.shapeType) {
            ShapeType.CUBE -> {
                showDimensionInputs()
                remove(radiusInput)
            }

            ShapeType.SPHERE -> {
                hideDimensionInputs()
                addDrawableChild(radiusInput)
            }

            ShapeType.CYLINDER -> {
                hideDimensionInputs()
                addDrawableChild(radiusInput)
                addDrawableChild(heightInput)
            }

            else -> throw IllegalArgumentException("Invalid shape type")
        }
    }

    private fun showDimensionInputs() {
        addDrawableChild(widthInput)
        addDrawableChild(heightInput)
        addDrawableChild(depthInput)
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