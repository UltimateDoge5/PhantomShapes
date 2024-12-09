package org.pkozak.ui.input

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.util.function.Consumer
import kotlin.math.min


/**
 * A wrapper around the `TextFieldWidget` that allows for easy positioning of an input field, and its label.
 * Also performs validation on the input using the constraints that can be provided.
 * The input can be scrolled to increase or decrease the value.
 *
 * @param textRenderer The text renderer used for rendering the text.
 * @param placeholder The placeholder text for the input, also used for the tooltip and the label.
 * @param value The initial value of the input.
 *
 * @see net.minecraft.client.gui.widget.TextFieldWidget
 */
class ShapePropertyInput(textRenderer: TextRenderer, private val placeholder: String, value: String) : ShapeInput,
    TextFieldWidget(textRenderer, 50, 20, Text.of(value)) {
    constructor(textRenderer: TextRenderer, label: String, value: String, constraints: Constraint) : this(
        textRenderer,
        label,
        value
    ) {
        this.constraints = constraints
    }

    constructor(textRenderer: TextRenderer, x: Int, y: Int, placeholder: String) : this(
        textRenderer,
        placeholder,
        placeholder
    ) {
        this.x = x
        this.y = y
    }

    // We need a setter as init fires before the label is set in the generator and the tooltip would not update.
    var label: String? = placeholder
        set(value) {
            field = value
            if (label === null || label !== placeholder) {
                super.tooltip = Tooltip.of(Text.of(placeholder))
            }
        }

    var tooltip: String? = placeholder
        set(value) {
            field = value
            super.tooltip = Tooltip.of(Text.of(value))
        }

    var constraints: Constraint? = Constraint(1, null)

    init {
        val text = Text.of(placeholder)
        super.setPlaceholder(text)
    }

    /**
     * Validates that the input - isn't empty, is a number and if it fits within the constraints (if any).
     * Upon an error returns it as a string
     */
    override fun checkForError(): String? {
        if (this.text.isEmpty()) {
            return "Shape ${placeholder.lowercase()} cannot be empty."
        }
        val value = this.text.toIntOrNull() ?: return "Shape ${placeholder.lowercase()} must be a number."

        if (constraints === null) return null

        if (constraints!!.min > value) {
            return "Shape ${placeholder.lowercase()} must be greater than or equal ${constraints!!.min}."
        }

        if (constraints!!.max != null && constraints!!.max!! < value) {
            return "Shape ${placeholder.lowercase()} must be less than or equal ${constraints!!.max}."
        }
        return null
    }

    override fun setChangedListener(listener: Consumer<String>) {
        super.setChangedListener {
            if (checkForError() !== null) return@setChangedListener
            listener.accept(this.text)
        }
    }

    override fun getLabel(): Label? {
        return if (label !== null) Label(label!!, this.x, this.y - 10) else null
    }

    override fun getValue(): String {
        return this.text
    }

    override fun setValue(value: String) {
        this.text = value
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        // Check for error first, after this we can be sure that the value is a number
        if (checkForError() !== null) return false

        val handle = MinecraftClient.getInstance().window.handle
        val isCtrlPressed = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL)
        val isShiftPressed = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT)

        // Before we change the value, we need to check if the value is within the constraints
        if (verticalAmount > 0.0) {
            val increment = if (isCtrlPressed) 100 else if (isShiftPressed) 10 else 1
            val newValue = text.toInt() + increment
            text = if (constraints!!.max != null) {
                min(newValue, constraints!!.max!!).toString()
            } else {
                newValue.toString()
            }
        } else {
            val decrement = if (isCtrlPressed) 100 else if (isShiftPressed) 10 else 1
            val newValue = text.toInt() - decrement
            text = if (constraints!!.min <= newValue) {
                newValue.toString()
            } else {
                constraints!!.min.toString()
            }
        }

        return true
    }

    /**
     * @param min The minimum value for the input (inclusive)
     * @param max The maximum value for the input (inclusive). If null, there is no maximum.
     */
    data class Constraint(val min: Int, val max: Int?)

    override fun forEachChild(consumer: Consumer<ClickableWidget>) {
        consumer.accept(this)
    }
}
