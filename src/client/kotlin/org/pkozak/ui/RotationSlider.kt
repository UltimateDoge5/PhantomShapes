package org.pkozak.ui

import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.text.Text

class RotationSlider(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val callback: (Double) -> Unit,
) :
    SliderWidget(x, y, width, height, Text.literal("0 deg"), 0.0) {
    init {
        this.updateMessage()
    }

    override fun updateMessage() {
        this.message = when (this.value) {
            0.0 -> Text.literal("0 deg")
            0.5 -> Text.literal("90 deg")
            1.0 -> Text.literal("270 deg")
            else -> Text.of("${(this.value * 360.0).toInt()} deg")
        }
    }

    // Clamp the value to 0, 0.5, or 1
    override fun applyValue() {
        this.value = when {
            this.value < 0.25 -> 0.0
            this.value < 0.75 -> 0.5
            else -> 1.0
        }
        // Call the callback with the correct angle value
        callback(
            when (this.value) {
                0.0 -> 0.0
                0.5 -> 90.0
                else -> 270.0
            }
        )
    }

    fun setAngle(angle: Double) {
        this.value = when (angle) {
            0.0 -> 0.0
            90.0 -> 0.5
            else -> 1.0
        }
        this.updateMessage()
    }

    fun getAngle() = when (this.value) {
        0.0 -> 0.0
        0.5 -> 90.0
        else -> 270.0
    }
}
