package org.pkozak.shape

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class Polygon(
    override var name: String,
    override var color: Color,
    override var pos: Vec3d,
    override var radius: Int,
    var height: Int,
    var sides: Int
) : RadialShape() {
    override val type = ShapeType.POLYGON

    override fun generateBlocks(): MutableSet<Vec3d> {
        if (sides < 3 || radius <= 0) return mutableSetOf()
        val vertices = mutableSetOf<Vec3i>()
        val blockPositions = mutableSetOf<Vec3d>()

        val angleIncrement = 2 * Math.PI / sides

        for (i in 0 until sides) {
            val theta = i * angleIncrement
            val x = pos.x + radius * cos(theta)
            val z = pos.z + radius * sin(theta)
            vertices.add(Vec3i(x.roundToInt(), pos.y.roundToInt(), z.roundToInt()))
        }

        // Connect the vertices
        for (i in 0 until sides) {
            val next = (i + 1) % sides // Modulo so we wrap around to the first vertex
            val vertex1 = vertices.elementAt(i)
            val vertex2 = vertices.elementAt(next)

            blockPositions.addAll(
                blockLine(
                    vertex1.x,
                    vertex1.z,
                    vertex2.x,
                    vertex2.z
                )
            )
        }

        // Scale with height
        val blockPositionsCopy = blockPositions.toMutableSet()
        for (i in 1 until height) {
            blockPositionsCopy.forEach { blockPositions.add(Vec3d(it.x, it.y + i, it.z)) }
        }

        // Offset the polygon by -1 in the x
        return blockPositions.map { Vec3d(it.x - 1, it.y, it.z) }.toMutableSet()
    }

    // Plot lines on the Minecraft's square grid between vertices using Bresenham's line algorithm
    private fun blockLine(x1: Int, z1: Int, x2: Int, z2: Int): MutableSet<Vec3d> {
        val blockPositions = mutableSetOf<Vec3d>()

        // Need to use var because we will be changing these values
        var x = x1
        var z = z1

        val dx = abs(x2 - x)
        val dz = abs(z2 - z)
        val sx = if (x < x2) 1 else -1
        val sz = if (z < z2) 1 else -1
        var err = dx - dz

        while (true) {
            blockPositions.add(Vec3d(x.toDouble(), pos.y, z.toDouble()))
            if (x == x2 && z == z2) break
            val e2 = 2 * err
            if (e2 > -dz) {
                err -= dz
                x += sx
            }
            if (e2 < dx) {
                err += dx
                z += sz
            }
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
            put("sides", sides)
            put("rotation", rotation.toJsonObject())
        }

        return json
    }
}