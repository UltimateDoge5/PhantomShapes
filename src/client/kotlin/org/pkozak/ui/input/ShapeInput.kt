package org.pkozak.ui.input

import net.minecraft.client.gui.widget.Widget
import java.util.function.Consumer

interface ShapeInput : Widget {
    fun checkForError(): String?
    fun getLabel(): Label?
    fun getValue(): String
    fun setValue(value: String, listener: Consumer<String>)
}

data class Label(val text: String, val x: Int, val y: Int)
