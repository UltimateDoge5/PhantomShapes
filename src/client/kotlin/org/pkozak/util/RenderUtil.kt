package org.pkozak.util

import net.minecraft.client.render.BufferBuilder
import org.joml.Matrix4f

class RenderUtil {
    companion object {
        fun buildQuad(
            buffer: BufferBuilder,
            matrix: Matrix4f,
            red: Float,
            green: Float,
            blue: Float,
            alpha: Float,
            x1: Float,
            y1: Float,
            z1: Float,
            x2: Float,
            y2: Float,
            z2: Float
        ) {
            buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha)
            buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha)
            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha)
            buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha)

            buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha)
            buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha)

            buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha)
            buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha)

            buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha)
            buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha)

            buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha)
            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha)

            buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha)
        }

        fun buildOutline(
            buffer: BufferBuilder,
            rotatedMatrix: Matrix4f,
            red: Float,
            green: Float,
            blue: Float,
            alpha: Float,
            x1: Float,
            y1: Float,
            z1: Float,
            x2: Float,
            y2: Float,
            z2: Float
        ) {
            buffer.vertex(rotatedMatrix, x1, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x1, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x1, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x2, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x2, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x2, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x2, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x1, y1, z1).color(red, green, blue, alpha)

            buffer.vertex(rotatedMatrix, x1, y2, z1).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x1, y2, z2).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x1, y2, z2).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x2, y2, z2).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x2, y2, z2).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x2, y2, z1).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x2, y2, z1).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x1, y2, z1).color(red, green, blue, alpha)

            buffer.vertex(rotatedMatrix, x1, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x1, y2, z1).color(red, green, blue, alpha)

            buffer.vertex(rotatedMatrix, x2, y1, z1).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x2, y2, z1).color(red, green, blue, alpha)

            buffer.vertex(rotatedMatrix, x2, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x2, y2, z2).color(red, green, blue, alpha)

            buffer.vertex(rotatedMatrix, x1, y1, z2).color(red, green, blue, alpha)
            buffer.vertex(rotatedMatrix, x1, y2, z2).color(red, green, blue, alpha)
        }
    }
}