package org.pkozak.ui.input

import net.minecraft.client.gui.widget.Widget
import java.util.function.Consumer


/**
 * Interface for all shape inputs used in the ShapeInputGenerator, used for validation and getting the value.
 * @see ShapeInputGenerator
 */
interface ShapeInput : Widget {
    fun checkForError(): String?
    fun getLabel(): Label?
    fun getValue(): String
    fun setValue(value: String, listener: Consumer<String>)
}

data class Label(val text: String, val x: Int, val y: Int)
