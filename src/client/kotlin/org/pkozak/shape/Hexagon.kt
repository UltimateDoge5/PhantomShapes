package org.pkozak.shape

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.pkozak.PhantomShapesClient.logger
import java.awt.Color
import kotlin.math.*

class Hexagon(
    override var name: String,
    override var color: Color,
    override var pos: Vec3d,
    override var radius: Int,
    var height: Int
) : RadialShape() {
    override val type = ShapeType.HEXAGON

    override fun generateBlocks(): MutableSet<Vec3d> {
        val hexagonVertices = mutableSetOf<Vec3i>()
        val blockPositions = mutableSetOf<Vec3d>()

        // Even radii are susceptible to rounding errors
        val correctedRadius = if (radius % 2 == 0) radius.toFloat() + 0.1f else radius.toFloat()

        for (i in 0 until 6) {
            val angle = i * Math.PI / 3 + Math.toRadians(rotation)
            val x = (correctedRadius * cos(angle) ).toInt()
            val z = (correctedRadius * sin(angle) ).toInt()
            hexagonVertices.add(Vec3i(x, pos.y.toInt(), z))
        }

        // Connect the vertices
        for (i in 0 until 6) {
            val next = (i + 1) % 6
            val vertex1 = hexagonVertices.elementAt(i)
            val vertex2 = hexagonVertices.elementAt(next)

            blockPositions.addAll(
                blockLine(
                    vertex1.x.toDouble(),
                    vertex1.z.toDouble(),
                    vertex2.x.toDouble(),
                    vertex2.z.toDouble()
                )
            )
        }

        val translatedBlocks = mutableSetOf<Vec3d>()

        // Translate the block to the correct position
        for (blockIdx in 0 until blockPositions.size) {
            val block = blockPositions.elementAt(blockIdx)
            translatedBlocks.add(Vec3d(block.x + pos.x - 1, block.y, block.z + pos.z))
        }

        for (layer in 1 until height) {
            for (blockIdx in 0 until translatedBlocks.size) {
                val block = translatedBlocks.elementAt(blockIdx)
                translatedBlocks.add(Vec3d(block.x, block.y + layer, block.z))
            }
        }

        return translatedBlocks
    }

    private fun blockLine(x1: Double, y1: Double, x2: Double, y2: Double): MutableSet<Vec3d> {
        val blockPositions = mutableSetOf<Vec3d>()
        val xStep = (x2 - x1) / 120
        val yStep = (y2 - y1) / 120

        for (i in 0 until 120) {
            blockPositions.add(Vec3d(round(x1 + i * xStep), pos.y, round(y1 + i * yStep)))
        }
        return blockPositions
    }

    override fun toJsonObject(): JsonObject {
        val json = buildJsonObject {
            put("name", name)
            put("type", type.toString())
            put("color", color.rgb)
            put("pos", buildJsonObject {
                put("x", pos.x)
                put("y", pos.y)
                put("z", pos.z)
            })
            put("radius", radius)
            put("height", height)
            put("enabled", enabled)
            put("rotation", rotation)
        }

        return json
    }
}