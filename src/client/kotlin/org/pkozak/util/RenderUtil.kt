package org.pkozak.util

import net.minecraft.client.gl.GlUsage
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.pkozak.Options
import org.pkozak.PhantomShapesClient.logger
import org.pkozak.PhantomShapesClient.options
import org.pkozak.shape.Shape

class RenderUtil {
    companion object {
        /** Responsible for rendering the shapes if no buffers/caches are present or when data has changed and a rerender is necessary. */
        fun populateShapeBuffer(
            tessellator: Tessellator,
            positionMatrix: Matrix4f,
            drawMode: Options.DrawMode,
            blocks: List<Vec3d>,
            shape: Shape,
            bufferCache: MutableMap<String, VertexBuffer>
        ) {
            require(drawMode == Options.DrawMode.EDGES || drawMode == Options.DrawMode.FACES) {
                "drawMode parameter must be either EDGES or FACES"
            }

            if (blocks.isEmpty()) {
                logger.debug("Shape ${shape.name} passed no blocks to render")
                return
            }

            val buffer = tessellator.begin(
                if (drawMode == Options.DrawMode.EDGES) DrawMode.DEBUG_LINES else DrawMode.QUADS,
                VertexFormats.POSITION_COLOR
            )

            val red = shape.color.red.toFloat() / 255
            val green = shape.color.green.toFloat() / 255
            val blue = shape.color.blue.toFloat() / 255

            for (block in blocks) {
                val start = block.subtract(shape.pos)
                val end = start.add(1.0, 1.0, 1.0)

                // Calculate the block center
                val centerX = (start.x + end.x) / 2.0
                val centerY = (start.y + end.y) / 2.0
                val centerZ = (start.z + end.z) / 2.0

                // Adjust start and end points based on blockSize
                val halfSize = 0.5 * options.blockSize
                val x1 = (centerX - halfSize).toFloat()
                val y1 = (centerY - halfSize).toFloat()
                val z1 = (centerZ - halfSize).toFloat()
                val x2 = (centerX + halfSize).toFloat()
                val y2 = (centerY + halfSize).toFloat()
                val z2 = (centerZ + halfSize).toFloat()

                if (drawMode == Options.DrawMode.EDGES) {
                    buildOutline(
                        buffer, positionMatrix, red, green, blue, options.outlineOpacity, x1, y1, z1, x2, y2, z2
                    )
                } else {
                    buildQuad(
                        buffer, positionMatrix, red, green, blue, options.fillOpacity, x1, y1, z1, x2, y2, z2
                    )
                }
            }

            // If we are re-rendering, make a new static buffer, if we are reordering re-use the old one
            val vertexBuffer = if (shape.shouldRerender || !bufferCache.containsKey(shape.name)) {
                val vbo = VertexBuffer(GlUsage.STATIC_WRITE)
                bufferCache[shape.name] = vbo
                vbo
            } else {
                bufferCache[shape.name]!!
            }

            vertexBuffer.bind()
            vertexBuffer.upload(buffer.end())
            VertexBuffer.unbind()
        }

        private fun buildQuad(
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

        private fun buildOutline(
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