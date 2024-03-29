package org.pkozak.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import org.pkozak.PhantomShapesClient
import org.pkozak.shape.Shape
import org.pkozak.util.SavedDataManager
import java.awt.Color

class ShapesScreen(private val parent: Screen?, internal val shapes: MutableList<Shape>) :
    Screen(Text.literal("Shapes manager")) {
    private var addShapeBtn: ButtonWidget? = null
    private var closeBtn: ButtonWidget? = null
    private var shapeListWidget: ShapeListWidget? = null

    override fun init() {
        shapeListWidget = ShapeListWidget(this, client!!, width, height, 60, 26, shapes)
        shapeListWidget!!.x = 0

        addShapeBtn =
            ButtonWidget.builder(Text.literal("New shape")) { client?.setScreen(ShapeEditorScreen(this, null)) }
                .dimensions(width / 2 - 205, height - 40, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Create a new phantom shape"))).build()
        closeBtn = ButtonWidget.builder(ScreenTexts.DONE) { close() }
            .dimensions(width / 2 + 5, height - 40, 200, 20).build()

        addDrawableChild(addShapeBtn)
        addDrawableChild(closeBtn)
        addDrawableChild(shapeListWidget)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, Color.WHITE.rgb)
        val y = 40

        // Draw the table text
        context.drawText(textRenderer, Text.literal("Name"), 40, y, Color.WHITE.rgb, false)
        context.drawText(textRenderer, Text.literal("Type"), 150, y, Color.WHITE.rgb, false)
        context.drawText(textRenderer, Text.literal("Color"), 190, y, Color.WHITE.rgb, false)
        context.drawText(textRenderer, Text.literal("Position"), 230, y, Color.WHITE.rgb, false)
        context.drawText(textRenderer, Text.literal("Blocks"), 360, y, Color.WHITE.rgb, false)
        context.drawText(textRenderer, Text.literal("Actions"), 408, y, Color.WHITE.rgb, false)

        shapeListWidget!!.render(context, mouseX, mouseY, delta)

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
        shapeListWidget!!.addEntry(ShapeListEntry(shapeListWidget!!, newShape, client!!))
    }
}