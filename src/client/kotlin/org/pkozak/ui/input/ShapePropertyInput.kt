package org.pkozak.ui.input

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import java.util.function.Consumer


/**
 * A wrapper around the `TextFieldWidget` that allows for easy positioning of an input field, and its label.
 * Also performs validation on the input using the constraints that can be provided.
 *
 * @param textRenderer The text renderer used for rendering the text.
 * @param placeholder The placeholder text for the input, also used for the tooltip and the label.
 * @param value The initial value of the input.
 *
 * @see net.minecraft.client.gui.widget.TextFieldWidget
 */
class ShapePropertyInput(textRenderer: TextRenderer, private val placeholder: String, value: String) : ShapeInput {
    constructor(textRenderer: TextRenderer, label: String, value: String, constraints: Constraint) : this(
        textRenderer,
        label,
        value
    ) {
        this.constraints = constraints
    }

    // We need a setter as init fires before the label is set in the generator and the tooltip would not update.
    var label: String? = placeholder
        set(value) {
            field = value
            if (label === null || label !== placeholder) {
                input.tooltip = Tooltip.of(Text.of(placeholder))
            }
        }

    var tooltip: String? = placeholder
        set(value) {
            field = value
            input.tooltip = Tooltip.of(Text.of(value))
        }
    private val input = TextFieldWidget(textRenderer, 50, 20, Text.of(value))
    private var constraints = Constraint(1, null)

    init {
        val text = Text.of(placeholder)
        input.setPlaceholder(text)
    }

    override fun checkForError(): String? {
        if (input.text.isEmpty()) {
            return "Shape ${placeholder.lowercase()} cannot be empty."
        }
        val value = input.text.toIntOrNull() ?: return "Shape ${placeholder.lowercase()} must be a number."

        if (constraints.min > value) {
            return "Shape ${placeholder.lowercase()} must be greater than or equal ${constraints.min}."
        }

        if (constraints.max != null && constraints.max!! < value) {
            return "Shape ${placeholder.lowercase()} must be less than or equal ${constraints.max}."
        }
        return null
    }

    override fun getLabel(): Label? {
        return if (label !== null) Label(label!!, input.x, input.y - 10) else null
    }

    override fun getValue(): String {
        return input.text
    }

    /**
     * @param listener The listener that will be called when the value changes and **is valid**
     */
    override fun setValue(value: String, listener: Consumer<String>) {
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
        return input.width
    }

    override fun getHeight(): Int {
        return input.height
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