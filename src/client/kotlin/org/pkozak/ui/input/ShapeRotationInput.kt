package org.pkozak.ui.input

import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.text.Text

import java.util.function.Consumer

class ShapeRotationInput(private val axis: Axis, private val showLabel: Boolean) : ShapeInput,
    PressableWidget(0, 0, 50, 20, Text.of("")) {
    private val texts = arrayOf("0 deg", "90 deg", "180 deg", "270 deg")
    private val degrees = arrayOf(0, 90, 180, 270)
    private var listener: Consumer<String>? = null
    private var degreeIndex = 0

    constructor(axis: Axis) : this(axis, false)

    override fun checkForError(): String? {
        return null
    }

    override fun getLabel(): Label? {
        return if (showLabel) Label("Rotation", x, y - 10) else null
    }

    override fun getValue(): String {
        return degrees[degreeIndex].toString()
    }

    override fun setValue(value: String, listener: Consumer<String>) {
        require(value.toIntOrNull() in degrees) { "Invalid rotation value" }
        degreeIndex = degrees.indexOf(value.toInt())
        this.listener = listener
        refreshTooltip()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        builder.put(NarrationPart.TITLE, this.narrationMessage)
        if (this.active) {
            val text = Text.of("Rotation of $axis axis by ${degrees[degreeIndex]} degrees")
            if (this.isFocused) {
                builder.put(
                    NarrationPart.USAGE,
                    Text.translatable("narration.cycle_button.usage.focused", *arrayOf<Any>(text))
                )
            } else {
                builder.put(
                    NarrationPart.USAGE,
                    Text.translatable("narration.cycle_button.usage.hovered", *arrayOf<Any>(text))
                )
            }
        }
    }

    override fun onPress() {
        degreeIndex = (degreeIndex + 1) % degrees.size
        listener?.accept(degrees[degreeIndex].toString())
        refreshTooltip()
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        if (verticalAmount > 0.0) {
            degreeIndex = (degreeIndex + 1) % degrees.size
        } else if (verticalAmount < 0.0) {
            degreeIndex = (degreeIndex - 1 + degrees.size) % degrees.size
        }
        listener?.accept(degrees[degreeIndex].toString())
        refreshTooltip()
        return true
    }

    private fun refreshTooltip() {
        message = Text.of(texts[degreeIndex])
        tooltip = Tooltip.of(Text.of("Rotation of $axis axis by ${degrees[degreeIndex]} degrees"))
    }

    enum class Axis {
        X, Y, Z
    }
}

